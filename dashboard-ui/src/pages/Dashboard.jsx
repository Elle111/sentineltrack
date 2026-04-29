import React, { useEffect } from 'react';
import Header from '../components/Header';
import RiskSummaryCards from '../components/RiskSummaryCards';
import AlertTable from '../components/AlertTable';
import SessionTable from '../components/SessionTable';
import { dashboardApi } from '../api/dashboardApi';

const Dashboard = () => {
  useEffect(() => {
    // Initialize mock data in the backend
    dashboardApi.getHealth().catch(console.error);
  }, []);

  return (
    <div style={dashboardStyle}>
      <Header />
      <div style={contentStyle}>
        <RiskSummaryCards />
        <AlertTable />
        <SessionTable />
      </div>
    </div>
  );
};

const dashboardStyle = {
  minHeight: '100vh',
  backgroundColor: '#f5f5f5'
};

const contentStyle = {
  maxWidth: '1200px',
  margin: '0 auto',
  padding: '20px'
};

export default Dashboard;
