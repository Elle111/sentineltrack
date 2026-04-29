import React from 'react';

const Header = () => {
  return (
    <header style={headerStyle}>
      <h1 style={titleStyle}>SentinelTrack</h1>
      <p style={subtitleStyle}>Real-time Security Monitoring Dashboard</p>
    </header>
  );
};

const headerStyle = {
  backgroundColor: '#1a1a2e',
  color: 'white',
  padding: '20px',
  textAlign: 'center'
};

const titleStyle = {
  margin: '0',
  fontSize: '28px',
  fontWeight: 'bold'
};

const subtitleStyle = {
  margin: '5px 0 0 0',
  fontSize: '14px',
  color: '#a0a0a0'
};

export default Header;
