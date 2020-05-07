import React from 'react';
import moment from 'moment';
import { EventType } from '../../features/notification/constants'

type titleProps = {
  title: string;
  type: string;
  time: number;
};

function getTitleText(title: string): JSX.Element {
  return (<span>{title.split("##").map((s, index) => {
    if (index % 2 === 1) {
      return <strong>{s}</strong>;
    }
    return s;
  })}</span>)
}

const ListTitle = ({ title, type, time }: titleProps) => {
  return (
    <div className="notification-title">
        {getTitleText(title)}
        <span>{moment(time).fromNow()}</span>
    </div>
  );
};

export default ListTitle;