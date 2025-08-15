// ======= API CONFIG =======
const API_BASE = "http://localhost:8080/api/v1"; // matches your CORS config
const AUTH_BASE = `${API_BASE}/auth`;

const TOKEN_KEY = "accessToken";
const EMAIL_KEY = "pendingEmail"; // used to carry email to verify page

// Save / load token
export function saveToken(token) { localStorage.setItem(TOKEN_KEY, token); }
export function getToken() { return localStorage.getItem(TOKEN_KEY); }
export function clearToken() { localStorage.removeItem(TOKEN_KEY); }

// Helpers
export function isAuthed() { return !!getToken(); }

export function authHeader() {
  const t = getToken();
  return t ? { "Authorization": `Bearer ${t}` } : {};
}

// Unified fetch with error decoding (plain text or JSON)
export async function apiFetch(url, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };
  const res = await fetch(url, { ...options, headers });

  // Try to read once
  let bodyText = await res.text();
  let bodyJson;
  try {
    bodyJson = bodyText ? JSON.parse(bodyText) : null;
  } catch {
    bodyJson = null; // not JSON
  }

  if (!res.ok) {
    const msg = bodyJson?.message || bodyText || `HTTP ${res.status}`;
    const err = new Error(msg);
    err.status = res.status;
    throw err;
  }

  return bodyJson !== null ? bodyJson : bodyText;
}


// JWT decode (no external libs)
export function decodeJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const payload = decodeURIComponent(
      atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
    );
    return JSON.parse(payload);
  } catch {
    return null;
  }
}

// Guard for protected pages
export function requireAuth() {
  const token = getToken();
  if (!token) {
    window.location.replace("index.html?m=Please%20log%20in");
    return;
  }
}

// Session expiry handler for protected calls
export function handleAuthError(e) {
  if ([401, 403, 504].includes(e.status)) {
    clearToken();
    window.location.replace("index.html?m=Session%20expired%2C%20please%20log%20in");
  } else {
    throw e;
  }
}

// ======= AUTH API CALLS =======
export async function registerUser({ username, email, password, role }) {
  const data = await apiFetch(`${AUTH_BASE}/register`, {
    method: "POST",
    body: JSON.stringify({ username, email, password, role })
  });
  if (data?.token) saveToken(data.token);
  localStorage.setItem(EMAIL_KEY, email);
  return data;
}

export async function verifyUser({ email, verificationCode }) {
  return apiFetch(`${AUTH_BASE}/verify`, {
    method: "POST",
    body: JSON.stringify({ email, verificationCode })
  });
}

export async function resendCode(email) {
  return apiFetch(`${AUTH_BASE}/resend?email=${encodeURIComponent(email)}`, {
    method: "POST"
  });
}

export async function loginUser({ email, password }) {
  const data = await apiFetch(`${AUTH_BASE}/login`, {
    method: "POST",
    body: JSON.stringify({ email, password })
  });
  if (data?.token) saveToken(data.token);
  return data;
}

export function getPendingEmail() { return localStorage.getItem(EMAIL_KEY) || ""; }
export function clearPendingEmail() { localStorage.removeItem(EMAIL_KEY); }
