import axios from 'axios';

const client = axios.create({
  baseURL: 'https://solariq-backend-wcw6.onrender.com/api/v1',
  timeout: 30000,
});

export async function createAssessment(payload) {
  const { data } = await client.post('/assess', payload);
  return data;
}

export async function captureContact(assessmentId, payload) {
  const { data } = await client.post(`/assess/${assessmentId}/capture`, payload);
  return data;
}

export async function getLeads({ status, state, minScore, auth } = {}) {
  const params = {};
  if (status) params.status = status;
  if (state) params.state = state;
  if (minScore != null) params.minScore = minScore;
  const config = { params };
  if (auth) config.auth = auth;
  const { data } = await client.get('/leads', config);
  return data;
}

export async function updateLeadStatus(leadId, payload, auth) {
  const config = {};
  if (auth) config.auth = auth;
  const { data } = await client.patch(`/leads/${leadId}/status`, payload, config);
  return data;
}

export default { createAssessment, captureContact, getLeads, updateLeadStatus };
