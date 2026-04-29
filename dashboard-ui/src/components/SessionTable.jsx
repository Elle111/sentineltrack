import React, { useState, useEffect } from 'react';
import { dashboardApi } from '../api/dashboardApi';

const SessionTable = () => {
  const [sessions, setSessions] = useState([]);

  useEffect(() => {
    loadSessions();
  }, []);

  const loadSessions = async () => {
    try {
      const data = await dashboardApi.getSessions();
      setSessions(data);
    } catch (error) {
      console.error('Error loading sessions:', error);
    }
  };

  const getRiskColor = (score) => {
    if (score >= 70) return '#e74c3c';
    if (score >= 30) return '#f39c12';
    return '#27ae60';
  };

  return (
    <div style={containerStyle}>
      <h2 style={headingStyle}>Recent Sessions</h2>
      <table style={tableStyle}>
        <thead>
          <tr>
            <th style={thStyle}>User ID</th>
            <th style={thStyle}>IP Address</th>
            <th style={thStyle}>Location</th>
            <th style={thStyle}>Risk Score</th>
            <th style={thStyle}>Status</th>
            <th style={thStyle}>Start Time</th>
          </tr>
        </thead>
        <tbody>
          {sessions.map((session) => (
            <tr key={session.sessionId}>
              <td style={tdStyle}>{session.userId}</td>
              <td style={tdStyle}>{session.ipAddress}</td>
              <td style={tdStyle}>{session.city}, {session.country}</td>
              <td style={tdStyle}>
                <span style={{
                  ...badgeStyle,
                  backgroundColor: getRiskColor(session.riskScore)
                }}>
                  {session.riskScore}
                </span>
              </td>
              <td style={tdStyle}>
                <span style={{
                  ...badgeStyle,
                  backgroundColor: session.isActive ? '#27ae60' : '#95a5a6'
                }}>
                  {session.isActive ? 'Active' : 'Inactive'}
                </span>
              </td>
              <td style={tdStyle}>{new Date(session.startTime).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

const containerStyle = {
  backgroundColor: 'white',
  padding: '20px',
  borderRadius: '8px',
  margin: '20px 0',
  boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
};

const headingStyle = {
  margin: '0 0 20px 0',
  fontSize: '20px',
  color: '#333'
};

const tableStyle = {
  width: '100%',
  borderCollapse: 'collapse'
};

const thStyle = {
  padding: '12px',
  textAlign: 'left',
  borderBottom: '2px solid #ddd',
  backgroundColor: '#f8f9fa'
};

const tdStyle = {
  padding: '12px',
  borderBottom: '1px solid #ddd'
};

const badgeStyle = {
  color: 'white',
  padding: '4px 8px',
  borderRadius: '4px',
  fontSize: '12px',
  fontWeight: 'bold'
};

export default SessionTable;
