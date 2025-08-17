// ======= API CONFIG =======
const API_BASE = "http://localhost:8080/api/v1";
const AUTH_BASE = `${API_BASE}/auth`;

const TOKEN_KEY = "accessToken";
const EMAIL_KEY = "pendingEmail"; // used for verify page

// ========== TOKEN HELPERS ==========
export function saveToken(token) { localStorage.setItem(TOKEN_KEY, token); }
export function getToken() { return localStorage.getItem(TOKEN_KEY); }
export function clearToken() { localStorage.removeItem(TOKEN_KEY); }
export function isAuthed() { return !!getToken(); }

export function authHeader() {
  const t = getToken();
  return t ? { "Authorization": `Bearer ${t}` } : {};
}

// ========== FETCH WRAPPER ==========
export async function apiFetch(url, options = {}) {
  const isPublic = url.includes("/auth/register") || url.includes("/auth/login") || url.includes("/auth/verify") || url.includes("/auth/resend");

  const headers = {
    "Content-Type": "application/json",
    ...(isPublic ? {} : authHeader()),  // do not attach token for public endpoints
    ...(options.headers || {})
  };

  const res = await fetch(url, { ...options, headers });

  let bodyText = await res.text();
  let bodyJson;
  try {
    bodyJson = bodyText ? JSON.parse(bodyText) : null;
  } catch {
    bodyJson = null;
  }

  if (!res.ok) {
    const msg = bodyJson?.message || bodyText || `HTTP ${res.status}`;
    const err = new Error(msg);
    err.status = res.status;
    throw err;
  }

  return bodyJson !== null ? bodyJson : bodyText;
}

// ========== JWT DECODE ==========
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

// ========== AUTH HELPERS ==========
export function requireAuth() {
  const token = getToken();
  if (!token) {
    window.location.replace("../index.html?m=Please%20log%20in");
    return;
  }
}

export function handleAuthError(e) {
  if ([401, 403, 504].includes(e.status)) {
    clearToken();
    window.location.replace("../index.html?m=Session%20expired%2C%20please%20log%20in");
  } else {
    throw e;
  }
}

// ========== AUTH API ==========
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

// ========== PATIENT API ==========
const PATIENT_BASE = `${API_BASE}/patient`;

export async function createPatientProfile(patientData) {
  return apiFetch(`${PATIENT_BASE}`, {
    method: "POST",
    body: JSON.stringify(patientData)
  });   ``
}

export async function getPatientProfile() {
  return apiFetch(`${PATIENT_BASE}`, { method: "GET" });
}

export async function updatePatientProfile(patientData) {
  return apiFetch(`${PATIENT_BASE}`, {
    method: "PUT",
    body: JSON.stringify(patientData)
  });
}


// ========== ADMIN API ==========
const ADMIN_BASE = `${API_BASE}/admin`;

export async function getPendingDoctors() {
  return apiFetch(`${ADMIN_BASE}/pending-approval`, { method: "GET" });
}

export async function approveDoctor(doctorId) {
  return apiFetch(`${ADMIN_BASE}/approve-doctor/${doctorId}`, {
    method: "POST"
  });
}

export async function rejectDoctor(doctorId) {
  return apiFetch(`${ADMIN_BASE}/reject-doctor/${doctorId}`, {
    method: "POST"
  });
}


// ========== DOCTOR API ==========
const DOCTOR_BASE = `${API_BASE}/doctor`;

// Create doctor profile (RegisterDoctorDto) — includes registerNumber
export async function createDoctorProfile(doctorData) {
  return apiFetch(`${DOCTOR_BASE}`, {
    method: "POST",
    body: JSON.stringify(doctorData)
  });
}

// Get doctor profile
export async function getDoctorProfile() {
  return apiFetch(`${DOCTOR_BASE}`, { method: "GET" });
}

// Update doctor profile (UpdateDoctorDto) — WITHOUT registerNumber
export async function updateDoctorProfile(doctorData) {
  return apiFetch(`${DOCTOR_BASE}`, {
    method: "PUT",
    body: JSON.stringify(doctorData)
  });
}

