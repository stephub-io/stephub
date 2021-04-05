CREATE TABLE IF NOT EXISTS runner_tasks (
  task_name varchar(40) not null,
  task_instance varchar(80) not null,
  task_data blob,
  execution_time timestamp(6) not null,
  picked BOOLEAN not null,
  picked_by varchar(50),
  last_success timestamp(6) null,
  last_failure timestamp(6) null,
  consecutive_failures INT,
  last_heartbeat timestamp(6) null,
  version BIGINT not null,
  PRIMARY KEY (task_name, task_instance)
);

CREATE TABLE IF NOT EXISTS mgmt_tasks (
  task_name varchar(40) not null,
  task_instance varchar(80) not null,
  task_data blob,
  execution_time timestamp(6) not null,
  picked BOOLEAN not null,
  picked_by varchar(50),
  last_success timestamp(6) null,
  last_failure timestamp(6) null,
  consecutive_failures INT,
  last_heartbeat timestamp(6) null,
  version BIGINT not null,
  PRIMARY KEY (task_name, task_instance)
);