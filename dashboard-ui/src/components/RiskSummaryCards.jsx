import React, { useState, useEffect } from 'react';
import { dashboardApi } from '../api/dashboardApi';

const RiskSummaryCards = () => {
  const [stats, setStats] = useState({
    totalSessions: 0,
    highRiskSessions: 0,
    criticalAlerts: 0
  });

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const [sessions, alerts] = await Promise.all([
        dashboardApi.getSessions(),
        dashboardApi.getAlerts()
      ]);

      const totalSessions = sessions.length;
      const highRiskSessions = sessions.filter(s => s.riskScore >= 70).length;
      const criticalAlerts = alerts.filter(a => a.severity === 'CRITICAL').length;

      setStats({
        totalSessions,
        highRiskSessions,
        criticalAlerts
      });
    } catch (error) {
      console.error('Error loading stats:', error);
    }
  };

  return (
    <div style={cardsContainer}>
      <div style={cardStyle}>
        <h3 style={cardTitleStyle}>Total Sessions</h3>
        <p style={cardValueStyle}>{stats.totalSessions}</p>
      </div>
      <div style={{...cardStyle, borderColor: '#f39c12'}}>
        <h3 style={cardTitleStyle}>High Risk Sessions</h3>
        <p style={cardValueStyle}>{stats.highRiskSessions}</p>
      </div>
      <div style={{...cardStyle, borderColor: '#e74c3c'}}>
        <h3 style={cardTitleStyle}>Critical Alerts</h3>
        <p style={cardValueStyle}>{stats.criticalAlerts}</p>
      </div>
    </div>
  );
};

const cardsContainer = {
  display: 'flex',
  gap: '20px',
  justifyContent: 'center',
  margin: '20px 0'
};

const cardStyle = {
  backgroundColor: 'white',
  padding: '20px',
  borderRadius: '8px',
  minWidth: '200px',
  textAlign: 'center',
  border: '2px solid #3498db',
  boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
};

const cardTitleStyle = {
  margin: '0 0 10px 0',
  fontSize: '16px',
  color: '#555'
};

const cardValueStyle = {
  margin: '0',
  fontSize: '36px',
  fontWeight: 'bold',
  color: '#333'
};

export default RiskSummaryCards;
