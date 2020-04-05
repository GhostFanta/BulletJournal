import React from 'react';
import { Layout } from 'antd';

const { Footer } = Layout;

class FooterLayout extends React.Component {
  render() {
    return (
      <Footer style={{ textAlign: 'center' }}>
        Bullet Journal ©2020 Created by 1024 BBS
      </Footer>
    );
  }
}

export default FooterLayout;
