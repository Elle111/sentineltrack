import React, { useState, useEffect } from 'react';
import { dashboardApi } from '../api/dashboardApi';

const AlertTable = () => {
  const [alerts, setAlerts] = useState([]);

  useEffect(() => {
    loadAlerts();
  }, []);

  const loadAlerts = async () => {
    try {
      const data = await dashboardApi.getAlerts();
      setAlerts(data);
    } catch (error) {
      console.error('Error loading alerts:', error);
    }
  };

  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'CRITICAL': return '#e74c3c';
      case 'HIGH': return '#f39c12';
      case 'MEDIUM': return '#f1c40f';
      case 'LOW': return '#3498db';
      default: return '#95a5a6';
    }
  };

  return (
    <div style={containerStyle}>
      <h2 style={headingStyle}>Recent Alerts</h2>
      <table style={tableStyle}>
        <thead>
          <tr>
            <th style={thStyle}>User ID</th>
            <th style={thStyle}>Severity</th>
            <th style={thStyle}>Risk Score</th>
            <th style={thStyle}>Message</th>
            <th style={thStyle}>Timestamp</th>
          </tr>
        </thead>
        <tbody>
          {alerts.map((alert) => (
            <tr key={alert.alertId}>
              <td style={tdStyle}>{alert.userId}</td>
              <td style={tdStyle}>
                <span style={{
                  ...badgeStyle,
                  backgroundColor: getSeverityColor(alert.severity)
                }}>
                  {alert.severity}
                </span>
              </td>
              <td style={tdStyle}>{alert.riskScore}</td>
              <td style={tdStyle}>{alert.message}</td>
              <td style={tdStyle}>{new Date(alert.timestamp).toLocaleString()}</td>
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

export default AlertTable;
