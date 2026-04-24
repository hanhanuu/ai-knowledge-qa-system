const state = {
    lastTrace: null,
    recentTraces: [],
    metrics: null
};

document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    refreshTrace();
    refreshMetrics();
    document.getElementById("session-id").value = "demo-session";
});

function bindEvents() {
    document.getElementById("ingest-btn").addEventListener("click", ingestDocument);
    document.getElementById("ask-btn").addEventListener("click", askQuestion);
    document.getElementById("refresh-trace").addEventListener("click", refreshTrace);
    document.getElementById("seed-demo").addEventListener("click", loadDemoKnowledge);
}

async function loadDemoKnowledge() {
    document.getElementById("doc-title").value = "RAG 检索优化说明";
    document.getElementById("doc-meta").value = "domain=ai;owner=backend";
    document.getElementById("doc-content").value = [
        "RAG 系统常见问题之一是用户提问过短或表达过于模糊，导致检索阶段召回质量不足。",
        "查询重写可以补充缺失的领域关键词，提升问题表达质量与检索精度。",
        "语义分块比固定长度切分更能保留上下文信息，降低知识碎片化带来的语义损失。",
        "混合检索通常结合关键词召回与向量召回，兼顾精确匹配与语义相似度。",
        "RRF 融合排序可以把多路召回结果合并成更稳定的最终候选集合。"
    ].join(" ");
    showToast("已填充演示知识。");
}

async function ingestDocument() {
    const title = document.getElementById("doc-title").value.trim();
    const content = document.getElementById("doc-content").value.trim();
    const metadata = parseMetadata(document.getElementById("doc-meta").value.trim());

    if (!title || !content) {
        showToast("标题和内容不能为空。", true);
        return;
    }

    try {
        const result = await api("/api/knowledge/ingest", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({title, content, metadata})
        });
        document.getElementById("ingest-result").innerHTML = `
            <h3>导入结果</h3>
            <ul>
                <li>文档 ID：${escapeHtml(result.documentId)}</li>
                <li>当前文档数：${escapeHtml(String(result.documents))}</li>
                <li>生成分块数：${escapeHtml(String(result.chunks))}</li>
                <li>总分块数：${escapeHtml(String(result.totalChunks))}</li>
            </ul>
        `;
        showToast("知识导入成功。");
    } catch (error) {
        showToast(error.message || "知识导入失败。", true);
    }
}

async function askQuestion() {
    const question = document.getElementById("question-input").value.trim();
    const forceRetrieval = document.getElementById("force-retrieval").checked;
    const sessionId = document.getElementById("session-id").value.trim();

    if (!question) {
        showToast("请输入问题。", true);
        return;
    }

    try {
        const result = await api("/api/qa/ask", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({question, forceRetrieval, sessionId})
        });

        document.getElementById("answer-card").innerHTML = `
            <h3>回答结果</h3>
            <p><strong>会话 ID：</strong>${escapeHtml(result.sessionId)}</p>
            <p><strong>路由结果：</strong>${escapeHtml(result.route)}</p>
            <p><strong>重写后的查询：</strong>${escapeHtml(result.rewrittenQuery)}</p>
            <p><strong>记忆轮次数：</strong>${escapeHtml(String(result.memoryTurnCount))}</p>
            <p>${escapeHtml(result.answer)}</p>
            ${renderSuggestions(result.suggestions || [])}
        `;

        document.getElementById("tool-card").innerHTML = `
            <h3>工具调用</h3>
            ${renderList(result.toolCalls || [], "暂未执行工具调用。")}
        `;

        document.getElementById("evidence-card").innerHTML = `
            <h3>检索证据</h3>
            ${renderEvidence(result.evidence || [])}
        `;

        await refreshTrace();
        await refreshMetrics();
        showToast("回答生成成功。");
    } catch (error) {
        showToast(error.message || "提问失败。", true);
    }
}

async function refreshTrace() {
    try {
        const [trace, traces] = await Promise.all([
            api("/api/qa/trace"),
            api("/api/observability/traces")
        ]);
        state.lastTrace = trace;
        state.recentTraces = traces || [];
        renderTrace(trace);
        renderRecentTraces(state.recentTraces);
    } catch (error) {
        renderTrace(null);
        renderRecentTraces([]);
    }
}

async function refreshMetrics() {
    try {
        const metrics = await api("/api/observability/metrics");
        state.metrics = metrics;
        renderMetrics(metrics);
    } catch (error) {
        renderMetrics(null);
    }
}

function renderTrace(trace) {
    const container = document.getElementById("trace-card");
    if (!trace) {
        container.innerHTML = `
            <div class="result-card">
                <h3>路由结果</h3>
                <p class="empty-text">暂未生成轨迹。</p>
            </div>
        `;
        return;
    }

    container.innerHTML = `
        <div class="result-card">
            <h3>路由结果</h3>
            <p>${escapeHtml(trace.route)}</p>
            <p><strong>原始问题：</strong>${escapeHtml(trace.originalQuery)}</p>
            <p><strong>重写后的查询：</strong>${escapeHtml(trace.rewrittenQuery)}</p>
        </div>
        <div class="result-card">
            <h3>执行步骤</h3>
            ${renderList(trace.steps || [], "暂未记录规划步骤。")}
        </div>
        <div class="result-card">
            <h3>证据快照</h3>
            ${renderList(trace.evidence || [], "暂未记录证据。")}
        </div>
    `;
}

function renderRecentTraces(traces) {
    const container = document.getElementById("recent-traces");
    if (!traces.length) {
        container.innerHTML = `
            <h3>最近轨迹</h3>
            <p class="empty-text">暂未记录历史轨迹。</p>
        `;
        return;
    }
    container.innerHTML = `
        <h3>最近轨迹</h3>
        <ul>
            ${traces.map(trace => `<li>${escapeHtml(trace.route)} | ${escapeHtml(trace.rewrittenQuery)} | ${escapeHtml(trace.createdAt)}</li>`).join("")}
        </ul>
    `;
}

function renderMetrics(metrics) {
    const container = document.getElementById("metrics-card");
    if (!metrics) {
        container.innerHTML = `
            <div class="result-card">
                <h3>指标概览</h3>
                <p class="empty-text">暂未生成指标。</p>
            </div>
        `;
        return;
    }
    container.innerHTML = `
        <div class="result-card">
            <h3>总体指标</h3>
            <ul>
                <li>总提问数：${escapeHtml(String(metrics.totalQuestions))}</li>
                <li>检索问答数：${escapeHtml(String(metrics.retrievalQuestions))}</li>
                <li>知识命中率：${escapeHtml(String(metrics.knowledgeHitRatePct))}%</li>
                <li>平均召回分块数：${escapeHtml(String(metrics.avgRetrievedChunks))}</li>
            </ul>
        </div>
        <div class="result-card">
            <h3>路由计数</h3>
            ${renderObjectList(metrics.routeCounters || {})}
        </div>
        <div class="result-card">
            <h3>工具调用计数</h3>
            ${renderObjectList(metrics.toolCounters || {})}
        </div>
    `;
}

async function api(url, options = {}) {
    const response = await fetch(url, options);
    const payload = await response.json();
    if (!response.ok || !payload.success) {
        throw new Error(payload.message || "请求失败。");
    }
    return payload.data;
}

function parseMetadata(raw) {
    if (!raw) {
        return {};
    }
    return raw.split(";").reduce((acc, item) => {
        const [key, value] = item.split("=");
        if (key && value) {
            acc[key.trim()] = value.trim();
        }
        return acc;
    }, {});
}

function renderEvidence(evidence) {
    if (!evidence.length) {
        return `<p class="empty-text">暂未返回证据。</p>`;
    }
    return `<ul>${evidence.map(item => `<li>${escapeHtml(item)}</li>`).join("")}</ul>`;
}

function renderSuggestions(items) {
    if (!items.length) {
        return "";
    }
    return `
        <div>
            <strong>后续追问建议</strong>
            <ul>${items.map(item => `<li>${escapeHtml(item)}</li>`).join("")}</ul>
        </div>
    `;
}

function renderList(items, emptyText) {
    if (!items.length) {
        return `<p class="empty-text">${escapeHtml(emptyText)}</p>`;
    }
    return `<ul>${items.map(item => `<li>${escapeHtml(item)}</li>`).join("")}</ul>`;
}

function renderObjectList(objectMap) {
    const entries = Object.entries(objectMap);
    if (!entries.length) {
        return `<p class="empty-text">暂未记录数据。</p>`;
    }
    return `<ul>${entries.map(([key, value]) => `<li>${escapeHtml(key)}：${escapeHtml(String(value))}</li>`).join("")}</ul>`;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

let toastTimer = null;
function showToast(message, isError = false) {
    const toast = document.getElementById("toast");
    toast.textContent = message;
    toast.classList.toggle("error", isError);
    toast.classList.add("show");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove("show"), 2400);
}
