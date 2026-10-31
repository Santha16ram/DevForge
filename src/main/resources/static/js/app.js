const $ = (id) => document.getElementById(id);

const state = {
  view: "home",
  project: null,
};

function fmtWhen(iso) {
  if (!iso) return "";
  if (Array.isArray(iso)) {
    const [y, m, d, h = 0, min = 0] = iso;
    return new Date(y, m - 1, d, h, min).toLocaleString(undefined, {
      month: "short", day: "numeric", hour: "2-digit", minute: "2-digit"
    });
  }
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return String(iso);
  return d.toLocaleString(undefined, { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });
}

async function api(path, options = {}) {
  const res = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (res.status === 204) {
    return null;
  }
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) {
    throw new Error((data && data.error) || "Request failed");
  }
  return data;
}

function showView(name) {
  state.view = name;
  ["home", "projects", "detail"].forEach((v) => {
    $("view-" + v).classList.toggle("hidden", v !== name);
  });
  document.querySelectorAll(".nav-btn").forEach((btn) => {
    btn.classList.toggle("is-on", btn.dataset.view === (name === "detail" ? "projects" : name));
  });
}

function setHead(title, sub) {
  $("page-title").textContent = title;
  $("page-sub").textContent = sub;
}

function emptyRow(cols, text) {
  return `<tr><td colspan="${cols}" class="muted">${escapeHtml(text)}</td></tr>`;
}

async function renderHome() {
  setHead("Home", "Counts, recent activity, and the projects already on file.");
  showView("home");
  const d = await api("/api/dashboard");
  const counts = d.statusCounts || {};
  const projects = d.projects || [];
  const activity = d.activity || [];
  $("view-home").innerHTML = `
    <div class="stats">
      <div class="stat"><b>${d.projectCount}</b><span>projects</span></div>
      <div class="stat"><b>${d.openCount}</b><span>open tasks</span></div>
      <div class="stat"><b>${d.taskCount}</b><span>tasks total</span></div>
      <div class="stat"><b>${d.snippetCount}</b><span>snippets</span></div>
    </div>
    <div class="grid-2">
      <article class="card">
        <h2>Projects</h2>
        <table>
          <thead><tr><th>Key</th><th>Title</th><th>Owner</th></tr></thead>
          <tbody>
            ${projects.length ? projects.map((p) => `
              <tr>
                <td><span class="key">${escapeHtml(p.projectKey)}</span></td>
                <td><button class="linkish" data-open="${p.id}">${escapeHtml(p.title)}</button></td>
                <td>${escapeHtml(p.ownerName)}</td>
              </tr>`).join("") : emptyRow(3, "Nothing here yet.")}
          </tbody>
        </table>
      </article>
      <article class="card">
        <h2>Recent activity</h2>
        <table>
          <tbody>
            ${activity.length ? activity.map((a) => `
              <tr>
                <td><span class="chip">${escapeHtml(a.kind)}</span></td>
                <td>${escapeHtml(a.text)}<small class="muted"> · ${escapeHtml(fmtWhen(a.when))}</small></td>
              </tr>`).join("") : emptyRow(2, "No activity yet.")}
          </tbody>
        </table>
        <p class="muted" style="margin-top:10px">Backlog ${counts.BACKLOG || 0} · doing ${counts.DOING || 0} · blocked ${counts.BLOCKED || 0} · done ${counts.DONE || 0}</p>
      </article>
    </div>`;
  $("view-home").querySelectorAll("[data-open]").forEach((btn) => {
    btn.addEventListener("click", () => openProject(Number(btn.dataset.open)));
  });
}

async function renderProjects() {
  setHead("Projects", "Key must be 2–6 capital letters. Email is checked with regex before save.");
  showView("projects");
  const projects = await api("/api/projects");
  $("view-projects").innerHTML = `
    <div class="toolbar">
      <p class="muted">${projects.length} on file</p>
      <button class="solid" id="new-project">New project</button>
    </div>
    <article class="card">
      <table>
        <thead><tr><th>Key</th><th>Title</th><th>Owner</th><th></th></tr></thead>
        <tbody>
          ${projects.length ? projects.map((p) => `
            <tr>
              <td><span class="key">${escapeHtml(p.projectKey)}</span></td>
              <td><button class="linkish" data-open="${p.id}">${escapeHtml(p.title)}</button><div class="muted">${escapeHtml(p.summary)}</div></td>
              <td>${escapeHtml(p.ownerName)}<div class="muted">${escapeHtml(p.ownerEmail)}</div></td>
              <td><button class="ghost danger" data-del="${p.id}">Delete</button></td>
            </tr>`).join("") : emptyRow(4, "Add a project to start.")}
        </tbody>
      </table>
    </article>`;
  $("new-project").onclick = () => openModal("New project", [
    field("projectKey", "Key (CAMP, LAB…)", "text"),
    field("title", "Title", "text"),
    field("ownerName", "Owner name", "text"),
    field("ownerEmail", "Owner email", "email"),
    field("summary", "Summary", "textarea"),
  ], async (body) => {
    await api("/api/projects", { method: "POST", body: JSON.stringify(body) });
    await renderProjects();
  });
  $("view-projects").querySelectorAll("[data-open]").forEach((btn) => {
    btn.addEventListener("click", () => openProject(Number(btn.dataset.open)));
  });
  $("view-projects").querySelectorAll("[data-del]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      if (!confirm("Delete this project and its tasks/snippets?")) return;
      await api("/api/projects/" + btn.dataset.del, { method: "DELETE" });
      await renderProjects();
    });
  });
}

async function openProject(id) {
  const project = await api("/api/projects/" + id);
  state.project = project;
  await renderDetail();
}

async function renderDetail() {
  const p = state.project;
  setHead(p.projectKey + " — " + p.title, p.ownerName + " · " + p.ownerEmail);
  showView("detail");
  const [board, snippets, tags, assignees] = await Promise.all([
    api("/api/projects/" + p.id + "/board"),
    api("/api/projects/" + p.id + "/snippets"),
    api("/api/projects/" + p.id + "/snippet-tags"),
    api("/api/projects/" + p.id + "/assignees"),
  ]);
  const cols = ["BACKLOG", "DOING", "BLOCKED", "DONE"];
  const labels = { BACKLOG: "Backlog", DOING: "Doing", BLOCKED: "Blocked", DONE: "Done" };
  $("view-detail").innerHTML = `
    <p class="muted">${escapeHtml(p.summary)}</p>
    <p class="people muted">People on tasks: ${
      assignees.length
        ? assignees.map((n) => `<span class="chip">${escapeHtml(n)}</span>`).join(" ")
        : "none yet"
    }</p>
    <div class="toolbar">
      <h2>Task board</h2>
      <div>
        <button class="ghost" id="edit-project">Edit project</button>
        <button class="solid" id="new-task">Add task</button>
      </div>
    </div>
    <div class="board">
      ${cols.map((c) => `
        <div class="col">
          <h3>${labels[c]} <span class="muted">${(board[c] || []).length}</span></h3>
          ${(board[c] || []).length ? (board[c] || []).map((t) => `
            <div class="ticket pri-${t.priority}">
              <strong>${escapeHtml(t.title)}</strong>
              <small>${escapeHtml(t.assignee)} · ${escapeHtml(String(t.priority).toLowerCase())}</small>
              <small>${escapeHtml(t.notes || "")}</small>
              <div style="margin-top:6px">
                <button class="ghost" data-edit-task="${t.id}">Edit</button>
                <button class="ghost danger" data-del-task="${t.id}">Delete</button>
              </div>
            </div>`).join("") : `<p class="muted">Empty</p>`}
        </div>`).join("")}
    </div>
    <div class="toolbar" style="margin-top:22px">
      <h2>Snippets</h2>
      <div>
        <input class="search" id="snip-q" placeholder="search (regex on server)">
        <button class="ghost" id="snip-go">Find</button>
        <button class="solid" id="new-snip">Add snippet</button>
      </div>
    </div>
    <p class="muted">Tags: ${tags.length ? tags.map((t) => `<span class="chip">${escapeHtml(t)}</span>`).join(" ") : "none yet"}</p>
    <div id="snip-list">
      ${renderSnippets(snippets)}
    </div>`;

  $("edit-project").onclick = () => openModal("Edit project", [
    field("title", "Title", "text", p.title),
    field("ownerName", "Owner name", "text", p.ownerName),
    field("ownerEmail", "Owner email", "email", p.ownerEmail),
    field("summary", "Summary", "textarea", p.summary),
  ], async (body) => {
    state.project = await api("/api/projects/" + p.id, { method: "PUT", body: JSON.stringify(body) });
    await renderDetail();
  });

  $("new-task").onclick = () => openModal("Add task", [
    field("title", "Title", "text"),
    field("assignee", "Assignee", "text", p.ownerName),
    field("status", "Status", "select", "BACKLOG", ["BACKLOG", "DOING", "BLOCKED", "DONE"]),
    field("priority", "Priority", "select", "NORMAL", ["LOW", "NORMAL", "HIGH"]),
    field("notes", "Notes", "textarea"),
  ], async (body) => {
    await api("/api/projects/" + p.id + "/tasks", { method: "POST", body: JSON.stringify(body) });
    await renderDetail();
  });

  $("view-detail").querySelectorAll("[data-edit-task]").forEach((btn) => {
    const ticket = findTask(board, Number(btn.dataset.editTask));
    btn.onclick = () => openModal("Edit task", [
      field("title", "Title", "text", ticket.title),
      field("assignee", "Assignee", "text", ticket.assignee),
      field("status", "Status", "select", ticket.status, ["BACKLOG", "DOING", "BLOCKED", "DONE"]),
      field("priority", "Priority", "select", ticket.priority, ["LOW", "NORMAL", "HIGH"]),
      field("notes", "Notes", "textarea", ticket.notes),
    ], async (body) => {
      await api("/api/tasks/" + ticket.id, { method: "PUT", body: JSON.stringify(body) });
      await renderDetail();
    });
  });
  $("view-detail").querySelectorAll("[data-del-task]").forEach((btn) => {
    btn.onclick = async () => {
      if (!confirm("Delete this task?")) return;
      await api("/api/tasks/" + btn.dataset.delTask, { method: "DELETE" });
      await renderDetail();
    };
  });

  $("new-snip").onclick = () => openModal("Add snippet", [
    field("title", "Title", "text"),
    field("language", "Language", "select", "java", ["java", "sql", "js", "html", "css", "python", "bash"]),
    field("tags", "Tags (comma separated)", "text", "jdbc"),
    field("body", "Code", "textarea"),
  ], async (body) => {
    await api("/api/projects/" + p.id + "/snippets", { method: "POST", body: JSON.stringify(body) });
    await renderDetail();
  });

  $("snip-go").onclick = runSearch;
  $("snip-q").addEventListener("keydown", (e) => {
    if (e.key === "Enter") runSearch();
  });
  bindSnippetButtons();
}

function renderSnippets(list) {
  if (!list.length) return `<p class="muted">No snippets yet.</p>`;
  return list.map((s) => `
    <article class="snippet">
      <strong>${escapeHtml(s.title)}</strong>
      <span class="chip">${escapeHtml(s.language)}</span>
      ${(s.tagSet || []).map((t) => `<span class="chip">${escapeHtml(t)}</span>`).join("")}
      <pre>${escapeHtml(s.body)}</pre>
      <button class="ghost" data-copy-snip="${s.id}">Copy</button>
      <button class="ghost" data-edit-snip="${s.id}">Edit</button>
      <button class="ghost danger" data-del-snip="${s.id}">Delete</button>
    </article>`).join("");
}

function findTask(board, id) {
  for (const col of Object.values(board)) {
    const hit = col.find((t) => t.id === id);
    if (hit) return hit;
  }
  return null;
}

async function runSearch() {
  const q = $("snip-q").value.trim();
  const p = state.project;
  try {
    const list = q
      ? await api("/api/projects/" + p.id + "/snippets/search?q=" + encodeURIComponent(q))
      : await api("/api/projects/" + p.id + "/snippets");
    $("snip-list").innerHTML = renderSnippets(list);
    bindSnippetButtons();
  } catch (err) {
    alert(err.message);
  }
}

function bindSnippetButtons() {
  $("view-detail").querySelectorAll("[data-edit-snip]").forEach(bindSnippetEdit);
  $("view-detail").querySelectorAll("[data-del-snip]").forEach(bindSnippetDelete);
  $("view-detail").querySelectorAll("[data-copy-snip]").forEach((btn) => {
    btn.onclick = async () => {
      const p = state.project;
      const all = await api("/api/projects/" + p.id + "/snippets");
      const s = all.find((x) => x.id === Number(btn.dataset.copySnip));
      if (s) {
        await navigator.clipboard.writeText(s.body);
        btn.textContent = "Copied";
        setTimeout(() => { btn.textContent = "Copy"; }, 1200);
      }
    };
  });
}

function bindSnippetEdit(btn) {
  btn.onclick = async () => {
    const p = state.project;
    const all = await api("/api/projects/" + p.id + "/snippets");
    const s = all.find((x) => x.id === Number(btn.dataset.editSnip));
    openModal("Edit snippet", [
      field("title", "Title", "text", s.title),
      field("language", "Language", "select", s.language, ["java", "sql", "js", "html", "css", "python", "bash"]),
      field("tags", "Tags (comma separated)", "text", s.tags),
      field("body", "Code", "textarea", s.body),
    ], async (body) => {
      await api("/api/snippets/" + s.id, { method: "PUT", body: JSON.stringify(body) });
      await renderDetail();
    });
  };
}

function bindSnippetDelete(btn) {
  btn.onclick = async () => {
    if (!confirm("Delete this snippet?")) return;
    await api("/api/snippets/" + btn.dataset.delSnip, { method: "DELETE" });
    await renderDetail();
  };
}

function field(name, label, type, value = "", options = []) {
  return { name, label, type, value, options };
}

function openModal(title, fields, onSubmit) {
  $("modal-title").textContent = title;
  $("modal-error").textContent = "";
  $("modal-fields").innerHTML = fields.map((f) => {
    if (f.type === "textarea") {
      return `<label>${escapeHtml(f.label)}<textarea name="${f.name}">${escapeHtml(f.value || "")}</textarea></label>`;
    }
    if (f.type === "select") {
      return `<label>${escapeHtml(f.label)}<select name="${f.name}">${
        f.options.map((o) => `<option ${o === f.value ? "selected" : ""}>${escapeHtml(o)}</option>`).join("")
      }</select></label>`;
    }
    return `<label>${escapeHtml(f.label)}<input name="${f.name}" type="${f.type}" value="${escapeHtml(f.value || "")}"></label>`;
  }).join("");
  $("modal").classList.remove("hidden");
  $("modal-form").onsubmit = async (e) => {
    e.preventDefault();
    const body = {};
    fields.forEach((f) => {
      body[f.name] = $("modal-form").elements[f.name].value;
    });
    try {
      await onSubmit(body);
      closeModal();
    } catch (err) {
      $("modal-error").textContent = err.message;
    }
  };
}

function closeModal() {
  $("modal").classList.add("hidden");
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, (c) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  }[c]));
}

$("modal-cancel").onclick = closeModal;
$("modal").addEventListener("click", (e) => {
  if (e.target === $("modal")) closeModal();
});
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") closeModal();
});

document.querySelectorAll(".nav-btn").forEach((btn) => {
  btn.addEventListener("click", () => {
    if (btn.dataset.view === "home") renderHome();
    else renderProjects();
  });
});

$("clock").textContent = new Date().toLocaleDateString(undefined, {
  weekday: "short", month: "short", day: "numeric"
});

renderHome().catch((err) => {
  $("view-home").innerHTML = `<p class="err">${escapeHtml(err.message)}</p>`;
});
