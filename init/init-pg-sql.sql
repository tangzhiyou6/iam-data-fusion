
CREATE TABLE ods_dsjsj.ods_dsjsj_sync_records (
      id             BIGSERIAL PRIMARY KEY,                -- 数据ID，自增主键
      system         VARCHAR(100) NOT NULL,                -- 系统名称（非空）
      biz_type       VARCHAR(100) NOT NULL,                 -- 业务类型（可根据实际调整长度）
      sub_biz_type       VARCHAR(100),                 -- 二级业务类型（可根据实际调整长度）
      source_id      VARCHAR(255) NOT NULL,                -- 源数据ID
      oss_file_path      TEXT NOT NULL,                        -- OSS文件路径
      file_name       VARCHAR(1024) NOT NULL,
      file_suffix       VARCHAR(64),
      file_size       INTEGER,
      parse_status   SMALLINT NOT NULL DEFAULT 0,          -- 解析状态：1=成功，0=失败
      fail_reason    TEXT,                                 -- 失败原因（成功时可为NULL）
      data_time     TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP -- 数据创建时间
);

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_sync_records IS '数据同步记录表';

DROP TABLE "ods_dsjsj"."ods_dsjsj_brxt_file_upload_records";
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_file_upload_records (
       id             BIGSERIAL PRIMARY KEY,
       biz_type       VARCHAR(100) NOT NULL,
       archive_file_path VARCHAR(1024) NOT NULL,
       archive_file_size bigint DEFAULT 0,
       archive_file_done  SMALLINT DEFAULT 0,
       file_id      VARCHAR(128) NOT NULL,
       file_path      VARCHAR(2048) NOT NULL,
       file_name      VARCHAR(512) NOT NULL,
       oss_file_path  TEXT NOT NULL,
       file_size       bigint DEFAULT 0,
       data_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE "ods_dsjsj"."ods_dsjsj_brxt_file_upload_records" OWNER TO "dsjsj";
-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_file_upload_records IS '百瑞信托数据上传OSS记录表';
-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_file_upload_records.id           IS '数据ID，自增主键';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_file_upload_records.biz_type     IS '业务类型';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_file_upload_records.archive_file_path     IS '压缩包文件名';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_file_upload_records.archive_file_size     IS '压缩包文件大小';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_file_upload_records.archive_file_done    IS '压缩包是否完成上传，0：是未完成，1：完成';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_file_upload_records.file_path    IS '文件相对路径';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_file_upload_records.file_id    IS '文件ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_file_upload_records.file_name    IS '文件名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_file_upload_records.file_size    IS '文件大小';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_file_upload_records.oss_file_path IS 'OSS路径';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_file_upload_records.data_time   IS '数据创建时间，默认当前时间';
CREATE INDEX idx_ods_dsjsj_brxt_file_upload_records_system_biz_time ON ods_dsjsj.ods_dsjsj_brxt_file_upload_records (biz_type);
CREATE INDEX idx_ods_dsjsj_brxt_file_upload_records_file_path ON ods_dsjsj.ods_dsjsj_brxt_file_upload_records (file_path);
CREATE INDEX idx_ods_dsjsj_brxt_file_upload_records_archive_file_path ON ods_dsjsj.ods_dsjsj_brxt_file_upload_records (archive_file_path);
CREATE INDEX idx_ods_dsjsj_brxt_file_upload_records_system_file_id ON ods_dsjsj.ods_dsjsj_brxt_file_upload_records (file_id);


-- 创新投资-基金业务信息化系统-投资项目估值报告表 建表语句
CREATE TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report (
    -- 主键ID
    source_id VARCHAR(255),
    -- 批次号
    batch_no TEXT,
    -- 项目名称
    project_name TEXT,
    -- 评估基准日
    valuation_base_date TEXT,
    -- 报告日期
    report_issue_date TEXT,
    -- 估值单位
    valuation_unit TEXT,
    -- 指标名称
    indicator_name TEXT,
    -- 指标值
    indicator_value TEXT,
    -- 创建时间（带默认当前时间戳，非空约束）
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report IS '创新投资-基金业务信息化系统-投资项目估值报告表';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report.id IS '主键id';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report.file_id IS 'ods_cxtz_jjyw_file_base表ID字段';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report.batch_no IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report.project_name IS '项目名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report.valuation_base_date IS '评估基准日';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report.report_issue_date IS '报告日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report.valuation_unit IS '估值单位';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report.indicator_name IS '指标名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report.indicator_value IS '指标值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_project_valuation_report.create_time IS '创建时间';


CREATE TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    source_id VARCHAR(255),
    -- 批次号
    batch_no TEXT,
    company_name TEXT,
    report_date TEXT,
    work_end_date TEXT,
    period_date TEXT,
    indicator_name TEXT,
    indicator_value TEXT,
    -- 创建时间（带默认当前时间戳，非空约束）
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE "ods_dsjsj"."ods_dsjsj_cxtz_jjyw_fdd_cash_flow" OWNER TO "dsjsj";
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow IS '创新投资-基金业务信息化系统-财务尽调-抽取现金流量表';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow.id IS '主键id';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow.source_id IS 'ods_cxtz_jjyw_file_base表fileId字段';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow.batch_no IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow.company_name IS '企业全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow.report_date IS '报告出具日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow.work_end_date IS '工作截止日';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow.period_date IS '财务统计期间';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow.indicator_name IS '标准化指标名/文档原名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow.indicator_value IS '指标数值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_cash_flow.create_time IS '数据时间';

CREATE TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    source_id VARCHAR(255),
    -- 批次号
    batch_no TEXT,
    company_name TEXT,
    report_date TEXT,
    work_end_date TEXT,
    period_date TEXT,
    indicator_name TEXT,
    indicator_value TEXT,
    -- 创建时间（带默认当前时间戳，非空约束）
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE "ods_dsjsj"."ods_dsjsj_cxtz_jjyw_fdd_balance_sheet" OWNER TO "dsjsj";
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet IS '创新投资-基金业务信息化系统-财务尽调-抽取资产负债表';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet.id IS '主键id';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet.source_id IS 'ods_cxtz_jjyw_file_base表fileId字段';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet.batch_no IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet.company_name IS '企业全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet.report_date IS '报告出具日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet.work_end_date IS '工作截止日';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet.period_date IS '财务统计期间';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet.indicator_name IS '标准化指标名/文档原名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet.indicator_value IS '指标数值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_balance_sheet.create_time IS '数据时间';

CREATE TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    source_id VARCHAR(255),
    -- 批次号
    batch_no TEXT,
    company_name TEXT,
    report_date TEXT,
    work_end_date TEXT,
    period_date TEXT,
    indicator_name TEXT,
    indicator_value TEXT,
    -- 创建时间（带默认当前时间戳，非空约束）
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE "ods_dsjsj"."ods_dsjsj_cxtz_jjyw_fdd_income_statement" OWNER TO "dsjsj";
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement IS '创新投资-基金业务信息化系统-财务尽调-抽取利润表';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement.id IS '主键id';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement.source_id IS 'ods_cxtz_jjyw_file_base表fileId字段';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement.batch_no IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement.company_name IS '企业全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement.report_date IS '报告出具日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement.work_end_date IS '工作截止日';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement.period_date IS '财务统计期间';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement.indicator_name IS '标准化指标名/文档原名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement.indicator_value IS '指标数值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_fdd_income_statement.create_time IS '数据时间';

CREATE TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_legal_due_diligence_report (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    source_id VARCHAR(255),
    -- 批次号
   batch_no TEXT,
   invested_company_name TEXT,
   project_full_name TEXT,
   legal_report_date TEXT,
   -- 创建时间（带默认当前时间戳，非空约束）
   create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE "ods_dsjsj"."ods_dsjsj_cxtz_jjyw_legal_due_diligence_report" OWNER TO "dsjsj";
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_legal_due_diligence_report IS '创新投资-基金业务信息化系统-法律尽调报告';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_legal_due_diligence_report.id IS '主键id';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_legal_due_diligence_report.source_id IS 'ods_cxtz_jjyw_file_base表fileId字段';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_legal_due_diligence_report.batch_no IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_legal_due_diligence_report.invested_company_name IS '被投企业全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_legal_due_diligence_report.project_full_name IS '完整投资项目名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_legal_due_diligence_report.legal_report_date IS '法律尽调报告出具时间';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_legal_due_diligence_report.create_time IS '数据时间';


CREATE TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    source_id VARCHAR(255),
    batch_no TEXT,
    meeting_name TEXT,
    meeting_time TEXT,
    proposal_name TEXT,
    opinion_content TEXT,
    participant_name TEXT,
    participant_role TEXT,
    participant_position TEXT,
    data_type TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes OWNER TO dsjsj;
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes IS '创新投资-基金业务信息化系统-会议纪要';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.id IS '主键id';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.source_id IS 'ods_cxtz_jjyw_file_base表fileId字段';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.batch_no IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.meeting_name IS '会议纪要全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.meeting_time IS '会议举办时间';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.proposal_name IS '议案完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.opinion_content IS '审议意见内容';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.participant_name IS '人员姓名';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.participant_role IS '人员角色/部门';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.participant_position IS '具体职位';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.data_type IS '冗余数据类型，proposals：议案列表；participants： 参会人列表';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_meeting_minutes.create_time IS '数据入库时间';

CREATE TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_base_info (
       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       source_id VARCHAR(255),
       batch_no TEXT,
       indicator_name TEXT,
       indicator_value TEXT,
       create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_base_info OWNER TO dsjsj;
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_base_info IS '创新投资-基金业务信息化系统-投资协议基本信息';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_base_info.id IS '主键id';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_base_info.source_id IS 'ods_cxtz_jjyw_file_base表fileId字段';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_base_info.batch_no IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_base_info.indicator_name IS '指标名称（如：投资方、被投方、投资金额、股权比例等）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_base_info.indicator_value IS '指标数值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_base_info.create_time IS '数据入库时间';


CREATE TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity (
      id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
      source_id VARCHAR(255),
      batch_no TEXT,
      data_type VARCHAR(50),
      account_no TEXT,
      account_name TEXT,
      bank_name TEXT,
      pay_type TEXT,
      pay_deadline TEXT,
      company_name TEXT,
      company_role TEXT,
      credit_code TEXT,
      address TEXT,
      name TEXT,
      phone TEXT,
      id_card TEXT,
      person_role TEXT,
      related_subject TEXT,
      create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 修改表所有者
ALTER TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity OWNER TO dsjsj;

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity IS '创新投资-基金业务信息化系统-投资协议签约主体信息';

-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.id IS '主键id';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.source_id IS 'ods_cxtz_jjyw_file_base表fileId字段';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.batch_no IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.data_type IS '数据类型(ACCOUNT:账户, COMPANY:企业, PERSON:个人)';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.account_no IS '银行账号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.account_name IS '账户户名';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.bank_name IS '开户行全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.pay_type IS '收付款类型';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.pay_deadline IS '付款期限';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.company_name IS '企业完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.company_role IS '企业角色(如:投资方/被投方)';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.credit_code IS '统一社会信用代码';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.address IS '企业注册地址';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.name IS '自然人姓名';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.phone IS '联系方式';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.id_card IS '身份证号码';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.person_role IS '个人角色(如:法定代表人/实控人)';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.related_subject IS '关联主体完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_entity.create_time IS '数据入库时间';

CREATE TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel (
       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       source_id VARCHAR(255),
       batch_no TEXT,
       source_data TEXT,
       targer_data TEXT,
       amount TEXT,
       currency TEXT,
       ratio TEXT,
       remark TEXT,
       create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel OWNER TO dsjsj;
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel IS '创新投资-基金业务信息化系统-投资协议交易关系信息';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel.id IS '主键id';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel.source_id IS 'ods_cxtz_jjyw_file_base表fileId字段';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel.batch_no IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel.source_data IS '源主体全称（流出方/投资方）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel.targer_data IS '目标主体全称（流入方/被投方）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel.amount IS '交易金额（原始文本）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel.currency IS '币种';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel.ratio IS '股权占比/持股比例';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel.remark IS '备注（枚举值【资金细分拆分原文】或原文摘要）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_cxtz_jjyw_investment_agreement_rel.create_time IS '数据入库时间';

-- 电能e拍
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_dnyp_waste_materials;
CREATE TABLE ods_dsjsj.ods_dsjsj_dnyp_waste_materials (
       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
       source_id VARCHAR(255),
       batch_no TEXT,
       file_name TEXT,
       declare_company TEXT,
       material_name TEXT,
       quantity TEXT,
       unit TEXT,
       specification TEXT,
       estimated_weight TEXT,
       create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE ods_dsjsj.ods_dsjsj_dnyp_waste_materials OWNER TO dsjsj;
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_dnyp_waste_materials IS '电能e拍-废料物资';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_dnyp_waste_materials.id IS '主键id';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_dnyp_waste_materials.source_id IS 'fileId字段';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_dnyp_waste_materials.batch_no IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_dnyp_waste_materials.file_name IS '文档完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_dnyp_waste_materials.declare_company IS '申报单位全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_dnyp_waste_materials.material_name IS '物料名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_dnyp_waste_materials.quantity IS '数量';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_dnyp_waste_materials.unit IS '计量单位';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_dnyp_waste_materials.specification IS '物料规格';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_dnyp_waste_materials.estimated_weight IS '预估重量(吨)';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_dnyp_waste_materials.create_time IS '数据入库时间';

-- 百瑞信托 非结构数据提取表
-- 建表语句
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_inh_request_approval;
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_project_review_comment;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_project_review_comment (
       id                          BIGSERIAL PRIMARY KEY,
       source_id                   VARCHAR(255),
       batch_no                    VARCHAR(255),
       project_name                VARCHAR(500),
       review_code                 VARCHAR(100),
       submit_department           VARCHAR(200),
       review_basis                TEXT,
       risk_level                  VARCHAR(50),
       review_conclusion           VARCHAR(500),
       borrower_repay_ability      TEXT,
       borrower_shareholder_bg     TEXT,
       borrower_operate_info       TEXT,
       borrower_finance_info       TEXT,
       borrower_overall_status     TEXT,
       guarantor_guarantee_capacity TEXT,
       guarantor_shareholder_bg    TEXT,
       guarantor_operate_info      TEXT,
       guarantor_finance_info      TEXT,
       guarantor_overall_status    TEXT,
       compliance_analysis         TEXT,
       trustee_duty_analysis       TEXT,
       trustee_risk_analysis       TEXT,
       transaction_compliance_analysis TEXT,
       income_cap_ratio            VARCHAR(255),
       data_type                   VARCHAR(50),
       create_time                 TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_project_review_comment OWNER TO dsjsj;
-- 为常用查询字段添加索引（可选，根据业务需求调整）
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_project_review_comment IS '项目评审意见书（固有+信托）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.source_id IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.batch_no IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.project_name IS '信托/项目全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.review_code IS '评审意见书编码';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.submit_department IS '提交部门';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.review_basis IS '评审依据文本';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.risk_level IS '风险等级';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.review_conclusion IS '评审结论';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.borrower_repay_ability IS '借款人还款能力分析';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.borrower_shareholder_bg IS '还款方股东背景';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.borrower_operate_info IS '还款方经营情况';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.borrower_finance_info IS '还款方财务情况';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.borrower_overall_status IS '还款方整体总结';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.guarantor_guarantee_capacity IS '担保人担保能力分析';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.guarantor_shareholder_bg IS '担保方股东背景';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.guarantor_operate_info IS '担保方经营情况';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.guarantor_finance_info IS '担保方财务情况';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.guarantor_overall_status IS '担保方整体总结';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.compliance_analysis IS '合规性分析全文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.trustee_duty_analysis IS '受托责任分析';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.trustee_risk_analysis IS '受托责任风险分析';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.transaction_compliance_analysis IS '交易模式合规法律分析';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.income_cap_ratio IS '收入资本比';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.data_type IS '业务类型';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_review_comment.create_time IS '创建时间';

DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_major_risk_report;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_major_risk_report (
                                                            id                  BIGSERIAL       PRIMARY KEY,
                                                            source_id           VARCHAR(64)     ,
                                                            batch_no            VARCHAR(64)     ,
                                                            report_name         TEXT            ,
                                                            report_date         DATE            ,
                                                            project_name        TEXT            ,
                                                            belong_department   VARCHAR(128)    ,
                                                            risk_description    TEXT            ,
                                                            handling_suggestion TEXT            ,
                                                            create_time         TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_major_risk_report OWNER TO dsjsj;
-- 字段注释
COMMENT ON TABLE  ods_dsjsj.ods_dsjsj_brxt_major_risk_report IS '项目重大风险信号报告单（固有+信托）';

COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_report.id                   IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_report.source_id            IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_report.batch_no             IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_report.report_name          IS '报告单完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_report.report_date          IS '报告日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_report.project_name         IS '项目完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_report.belong_department    IS '项目归属部门';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_report.risk_description     IS '风险信号描述完整原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_report.handling_suggestion  IS '处理建议完整原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_report.create_time          IS '创建时间';

DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning (
                                                            id                  BIGSERIAL       PRIMARY KEY,
                                                            source_id           VARCHAR(64)     ,
                                                            batch_no            VARCHAR(64)     ,
                                                            report_name         TEXT            ,
                                                            report_date         DATE            ,
                                                            project_name        TEXT            ,
                                                            belong_department   VARCHAR(128)    ,
                                                            risk_description    TEXT            ,
                                                            handling_suggestion TEXT            ,
                                                            create_time         TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning OWNER TO dsjsj;
-- 字段注释
COMMENT ON TABLE  ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning IS '项目预警信号报告单（固有+信托）';

COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning.id                   IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning.source_id            IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning.batch_no             IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning.report_name          IS '报告单完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning.report_date          IS '报告日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning.project_name         IS '项目完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning.belong_department    IS '项目归属部门';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning.risk_description     IS '风险信号描述完整原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning.handling_suggestion  IS '处理建议完整原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning.create_time          IS '创建时间';

-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_project_decision_committee;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_project_decision_committee (
       id                      BIGSERIAL       PRIMARY KEY,
       source_id               VARCHAR(64)     ,
       batch_no                VARCHAR(64)     ,
       topic_name              TEXT            ,
       review_time             TEXT            ,
       meeting_address         TEXT            ,
       meeting_form            TEXT            ,
       topic_content           TEXT            ,
       is_related_transaction  VARCHAR(32)     ,
       vote_precondition       TEXT            ,
       vote_result             TEXT            ,
       name                    VARCHAR(128)    ,
       role                    TEXT            ,
       create_time         TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_project_decision_committee OWNER TO dsjsj;
-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_project_decision_committee  IS '项目决策委员会会议纪要（固有+信托）';

-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.id                     IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.source_id              IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.batch_no               IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.topic_name             IS '议题完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.review_time            IS '审议时间原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.meeting_address        IS '会议地点原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.meeting_form           IS '召开方式原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.topic_content          IS '议题内容完整原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.is_related_transaction IS '是否涉及关联交易原文结论';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.vote_precondition      IS '表决前提条件原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.vote_result            IS '表决结果完整原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.name                   IS '人员姓名（来自meeting_member_list）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.role                   IS '人员身份、代参会、缺席原因等完整描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_project_decision_committee.create_time          IS '创建时间';

DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report (
       id                        BIGSERIAL       PRIMARY KEY,
       source_id                 VARCHAR(64)     ,
       batch_no                  VARCHAR(64)     ,
       project_trust_name        TEXT            ,
       -- project_type              VARCHAR(128)    ,
       investigate_target        TEXT            ,
       investigate_date          VARCHAR(64)     ,
       product_type              VARCHAR(128)    ,
       risk_level                VARCHAR(32)     ,
       target_company            TEXT            ,
       invest_appropriateness    TEXT            ,
       risk_isolation            TEXT            ,
       priority_subordinate      TEXT            ,
       difference_complement     TEXT            ,
       info_disclosure_support   TEXT            ,
       comply_regulation         TEXT            ,
       counterparty_rating       VARCHAR(64)     ,
       exit_path                 TEXT            ,
       create_time               TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report OWNER TO dsjsj;
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report IS '信托业务尽职调查报告（信托）';

-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.id                         IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.source_id                  IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.batch_no                   IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.project_trust_name         IS '项目/信托计划全称';
-- COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.project_type               IS '项目类型';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.investigate_target         IS '调查对象主体信息';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.investigate_date           IS '调查时间';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.product_type               IS '产品类型';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.risk_level                 IS '风险等级';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.target_company             IS '被调查公司全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.invest_appropriateness     IS '投资适当性管理原文描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.risk_isolation             IS '风险隔离相关说明';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.priority_subordinate       IS '优先/劣后级结构描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.difference_complement      IS '差额补足约定内容';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.info_disclosure_support    IS '是否足以支撑信息披露原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.comply_regulation          IS '是否符合“资管新规”要求原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.counterparty_rating        IS '交易对手的主体信用评级';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.exit_path                  IS '项目退出路径描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report.create_time                IS '创建时间';


-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report (
                                                                 id                        BIGSERIAL       PRIMARY KEY,
                                                                 source_id                 VARCHAR(64)     ,
                                                                 batch_no                  VARCHAR(64)     ,
                                                                 project_trust_name        TEXT            ,
                                                                 -- project_type              VARCHAR(128)    ,
                                                                 submit_department         VARCHAR(256)    ,
                                                                 submit_date               VARCHAR(64)     ,
                                                                 irr                       TEXT            ,
                                                                 roi                       TEXT            ,
                                                                 cover_capital_cost        TEXT            ,
                                                                 liquidity_match           TEXT            ,
                                                                 invest_term               TEXT            ,
                                                                 has_financial_statement   TEXT            ,
                                                                 create_time               TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report OWNER TO dsjsj;
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report IS '固有业务尽职调查报告（固有）';

-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.id                         IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.source_id                  IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.batch_no                   IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.project_trust_name         IS '项目/信托计划全称';
 -- COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.project_type               IS '项目类型';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.submit_department          IS '提交部门名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.submit_date                IS '提交日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.irr                        IS '内部收益率数值/原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.roi                        IS '投资回报率数值/原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.cover_capital_cost         IS '是否覆盖公司资金成本说明';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.liquidity_match            IS '与自有资金流动性匹配情况描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.invest_term                IS '投资期限原文描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.has_financial_statement    IS '是否有财务报表说明';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report.create_time                IS '创建时间';


-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_custody_agreement;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_custody_agreement (
                                                            id                      BIGSERIAL       PRIMARY KEY,
                                                            source_id               VARCHAR(64)     ,
                                                            batch_no                VARCHAR(64)     ,
                                                            contract_name           TEXT            ,
                                                            contract_code           VARCHAR(128)    ,
                                                            trustee                 TEXT            ,
                                                            custodian               TEXT            ,
                                                            sign_date               VARCHAR(64)     ,
                                                            custodian_fee_bank_info TEXT            ,
                                                            data_time               TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report OWNER TO dsjsj;
-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_custody_agreement  IS '保管协议（固有+信托）';

-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_custody_agreement.id                       IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_custody_agreement.source_id                IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_custody_agreement.batch_no                 IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_custody_agreement.contract_name            IS '保管协议完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_custody_agreement.contract_code            IS '合同编码';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_custody_agreement.trustee                  IS '受托人主体名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_custody_agreement.custodian                IS '保管机构完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_custody_agreement.sign_date                IS '协议签订日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_custody_agreement.custodian_fee_bank_info  IS '保管费收款账户完整原文信息';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_custody_agreement.data_time                IS '数据时间';

-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_trust_contract;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_trust_contract (
                                                         id              BIGSERIAL       PRIMARY KEY,
                                                         source_id       VARCHAR(64)     ,
                                                         batch_no        VARCHAR(64)     ,
                                                         contract_name   TEXT            ,
                                                         contract_code   VARCHAR(128)    ,
                                                         trustee         TEXT            ,
                                                         sign_date       VARCHAR(64)     ,
                                                         trust_scale     TEXT            ,
                                                         expected_term   TEXT            ,
                                                         create_time     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_trust_contract  IS '信托合同/固有合同（固有+信托）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report OWNER TO dsjsj;
-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_contract.id               IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_contract.source_id        IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_contract.batch_no         IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_contract.contract_name    IS '信托合同完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_contract.contract_code    IS '合同编码';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_contract.trustee          IS '受托人主体名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_contract.sign_date        IS '合同签署日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_contract.trust_scale      IS '信托规模原文描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_contract.expected_term    IS '预计期限原文描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_contract.create_time      IS '创建时间';

-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_loan_contract;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_loan_contract (
                                                         id                      BIGSERIAL       PRIMARY KEY,
                                                         source_id               VARCHAR(64)     ,
                                                         batch_no                VARCHAR(64)     ,
                                                         contract_name           TEXT            ,
                                                         contract_code           VARCHAR(128)    ,
                                                         borrower                TEXT            ,
                                                         lender                  TEXT            ,
                                                         sign_date               VARCHAR(64)     ,
                                                         loan_fund_source        TEXT            ,
                                                         trust_plan_name         TEXT            ,
                                                         loan_type               TEXT            ,
                                                         loan_principal          TEXT            ,
                                                         loan_term               TEXT            ,
                                                         loan_rate               TEXT            ,
                                                         borrower_account_name   TEXT            ,
                                                         borrower_bank           TEXT            ,
                                                         borrower_account_no     TEXT            ,
                                                         lender_account_name     TEXT            ,
                                                         lender_bank             TEXT            ,
                                                         lender_account_no       TEXT            ,
                                                         create_time             TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_loan_contract  IS '借款合同（固有+信托）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_loan_contract OWNER TO dsjsj;
-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.id                      IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.source_id               IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.batch_no                IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.contract_name           IS '借款合同完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.contract_code           IS '合同编码';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.borrower                IS '借款人主体全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.lender                  IS '贷款人主体全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.sign_date               IS '合同签署日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.loan_fund_source        IS '贷款资金来源原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.trust_plan_name         IS '信托计划完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.loan_type               IS '贷款种类';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.loan_principal          IS '贷款本金完整原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.loan_term               IS '贷款期限原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.loan_rate               IS '贷款利率原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.borrower_account_name   IS '借款人户名';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.borrower_bank           IS '借款人开户银行';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.borrower_account_no     IS '借款人银行账号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.lender_account_name     IS '贷款人户名';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.lender_bank             IS '贷款人开户银行';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.lender_account_no       IS '贷款人银行账号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_loan_contract.create_time             IS '创建时间';

-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_perpetual_bond;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_perpetual_bond (
                                                         id                                     BIGSERIAL       PRIMARY KEY,
                                                         source_id                              VARCHAR(64)     ,
                                                         batch_no                               VARCHAR(64)     ,
                                                         project_name                           TEXT            ,
                                                         business_type                          VARCHAR(64)     ,
                                                         company_name                           TEXT            ,
                                                         company_role                           VARCHAR(128)    ,
                                                         invest_contract_code                   VARCHAR(128)    ,
                                                         trust_contract_code                    VARCHAR(128)    ,
                                                         trust_contract_name                    TEXT            ,
                                                         repayment_order                        TEXT            ,
                                                         has_investor_put_right                 VARCHAR(16)     ,
                                                         has_issuer_call_right                  VARCHAR(16)     ,
                                                         has_unconditional_principal_interest   VARCHAR(16)     ,
                                                         data_type                              VARCHAR(64)     ,
                                                         create_time                            TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_perpetual_bond  IS '永续债发行文件（固有+信托）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_perpetual_bond OWNER TO dsjsj;
-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.id                                     IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.source_id                              IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.batch_no                               IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.project_name                           IS '永续债项目完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.business_type                          IS '业务类型（信托业务/固有业务/未识别）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.company_name                           IS '主体全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.company_role                           IS '角色（投资方/被投资方）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.invest_contract_code                   IS '投资合同编号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.trust_contract_code                    IS '信托合同编号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.trust_contract_name                    IS '信托合同完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.repayment_order                        IS '清偿顺序原文描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.has_investor_put_right                 IS '是否有投资人回售权';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.has_issuer_call_right                  IS '是否有发行人赎回权';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.has_unconditional_principal_interest   IS '是否有无条件本息支付';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.data_type                              IS '业务类型（集合冗余标记）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_perpetual_bond.create_time                            IS '创建时间';

-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report (
                                                         id                        BIGSERIAL       PRIMARY KEY,
                                                         source_id                 VARCHAR(64)     ,
                                                         batch_no                  VARCHAR(64)     ,
                                                         project_name              TEXT            ,
                                                         service_type             TEXT            ,
                                                         establish_date            VARCHAR(64)     ,
                                                         outstanding_scale         TEXT            ,
                                                         report_date               VARCHAR(64)     ,
                                                         risk_disclosure           TEXT            ,
                                                         current_period_distributed_profit   TEXT            ,
                                                         current_period_income     TEXT            ,
                                                         year_to_date_net_profit   TEXT            ,
                                                         year_to_date_income       TEXT            ,
                                                         create_time               TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report  IS '信托项目投后报告（信托）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report OWNER TO dsjsj;
-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.id                        IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.source_id                 IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.batch_no                  IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.project_name              IS '信托项目完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.service_type             IS '业务品种';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.establish_date            IS '信托成立日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.outstanding_scale         IS '存续规模原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.report_date               IS '报告所属期/报告日期';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.risk_disclosure           IS '风险揭示完整原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.current_period_income     IS '本期收入合计金额/描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.current_period_distributed_profit    IS '本期已分配信托利润（单位换算为元，纯数字）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.year_to_date_income       IS '本年收入合计金额/描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.year_to_date_net_profit   IS '本年净利润合计金额/描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report.create_time               IS '创建时间';

-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_payment_advice;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_payment_advice (
                                                         id                      BIGSERIAL       PRIMARY KEY,
                                                         source_id               VARCHAR(64)     ,
                                                         batch_no                VARCHAR(64)     ,
                                                         project_name            TEXT            ,
                                                         contract_code           VARCHAR(128)    ,
                                                         payment_deadline        TEXT            ,
                                                         total_payable_amount    TEXT            ,
                                                         create_time             TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_payment_advice  IS '信托项目付款通知单（信托）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_payment_advice OWNER TO dsjsj;
-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_payment_advice.id                      IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_payment_advice.source_id               IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_payment_advice.batch_no                IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_payment_advice.project_name            IS '信托项目完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_payment_advice.contract_code           IS '合同编号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_payment_advice.payment_deadline        IS '付款截止日期原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_payment_advice.total_payable_amount    IS '应付合计金额原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_payment_advice.create_time             IS '创建时间';

-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report (
                                                         id                      BIGSERIAL       PRIMARY KEY,
                                                         source_id               VARCHAR(64)     ,
                                                         batch_no                VARCHAR(64)     ,
                                                         project_name            TEXT            ,
                                                         establish_time          TEXT            ,
                                                         outstanding_scale       TEXT            ,
                                                         trust_credit_rating     TEXT            ,
                                                         create_time             TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report  IS '信息披露报告（信托）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report OWNER TO dsjsj;
-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report.id                      IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report.source_id               IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report.batch_no                IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report.project_name            IS '信托项目完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report.establish_time          IS '项目成立时间原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report.outstanding_scale       IS '存续规模原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report.trust_credit_rating     IS '信托项目信用等级';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report.create_time             IS '创建时间';

-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_request_report;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_request_report (
                                                         id                               BIGSERIAL       PRIMARY KEY,
                                                         source_id                        VARCHAR(64)     ,
                                                         batch_no                         VARCHAR(64)     ,
                                                         project_name                     TEXT            ,
                                                         establish_time                   TEXT            ,
                                                         project_type                     TEXT            ,
                                                         comply_asset_management_rule     TEXT            ,
                                                         create_time                      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_request_report  IS '信托项目请示报告（信托）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_request_report OWNER TO dsjsj;
-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_request_report.id                               IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_request_report.source_id                        IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_request_report.batch_no                         IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_request_report.project_name                     IS '信托项目完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_request_report.establish_time                   IS '项目成立时间原文';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_request_report.project_type                     IS '项目类型描述';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_request_report.comply_asset_management_rule     IS '是否符合资管新规原文结论';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_request_report.create_time                      IS '创建时间';

-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_income_statement;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_income_statement (
                                                         id                 BIGSERIAL       PRIMARY KEY,
                                                         source_id          VARCHAR(64)     ,
                                                         batch_no           VARCHAR(64)     ,
                                                         company_name       TEXT            ,
                                                         project_name       TEXT            ,
                                                         "date"             TEXT            ,
                                                         indicator_name     TEXT            ,
                                                         indicator_value    TEXT            ,
                                                         create_time        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_income_statement  IS '财务报告 - 利润表提取（固有 + 信托）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_income_statement OWNER TO dsjsj;
-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_income_statement.id                 IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_income_statement.source_id          IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_income_statement.batch_no           IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_income_statement.company_name       IS '企业全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_income_statement.project_name       IS '项目名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_income_statement."date"             IS '财务统计期间';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_income_statement.indicator_name     IS '标准化指标名/文档原名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_income_statement.indicator_value    IS '指标数值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_income_statement.create_time        IS '创建时间';

-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_balance_sheet;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_balance_sheet (
                                                         id                 BIGSERIAL       PRIMARY KEY,
                                                         source_id          VARCHAR(64)     ,
                                                         batch_no           VARCHAR(64)     ,
                                                         company_name       TEXT            ,
                                                         project_name       TEXT            ,
                                                         "date"             TEXT            ,
                                                         indicator_name     TEXT            ,
                                                         indicator_value    TEXT            ,
                                                         create_time        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_balance_sheet  IS '财务报告 - 资产负债表（固有 + 信托）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_balance_sheet OWNER TO dsjsj;
-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_balance_sheet.id                 IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_balance_sheet.source_id          IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_balance_sheet.batch_no           IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_balance_sheet.company_name       IS '企业全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_balance_sheet.project_name       IS '项目名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_balance_sheet."date"             IS '财务统计期间';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_balance_sheet.indicator_name     IS '标准化指标名/文档原名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_balance_sheet.indicator_value    IS '指标数值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_balance_sheet.create_time        IS '创建时间';

-- 建表
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement (
                                                         id                 BIGSERIAL       PRIMARY KEY,
                                                         source_id          VARCHAR(64)     ,
                                                         batch_no           VARCHAR(64)     ,
                                                         company_name       TEXT            ,
                                                         project_name       TEXT            ,
                                                         "date"             TEXT            ,
                                                         indicator_name     TEXT            ,
                                                         indicator_value    TEXT            ,
                                                         create_time        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 表注释
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement  IS '财务报告 - 现金流量表（固有 + 信托）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement OWNER TO dsjsj;
-- 字段注释
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement.id                 IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement.source_id          IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement.batch_no           IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement.company_name       IS '企业全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement.project_name       IS '项目名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement."date"             IS '财务统计期间';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement.indicator_name     IS '标准化指标名/文档原名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement.indicator_value    IS '指标数值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement.create_time        IS '创建时间';

-- 建表：投资协议合同 - 基本信息
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info (
                                                         id                 BIGSERIAL       PRIMARY KEY,
                                                         source_id          VARCHAR(64)     ,
                                                         batch_no           VARCHAR(64)     ,
                                                         scene    VARCHAR(256)     NULL,
                                                         indicator_name     TEXT            ,
                                                         indicator_value    TEXT            ,
                                                         create_time        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info  IS '投资协议合同 - 基本信息（信托+固有）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info OWNER TO dsjsj;
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info.id                 IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info.source_id          IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info.batch_no           IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info.scene     IS '业务类型';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info.indicator_name     IS '指标名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info.indicator_value    IS '指标数值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info.create_time        IS '创建时间';

-- 建表：投资协议合同 - 实体信息
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity (
                                                         id                 BIGSERIAL       PRIMARY KEY,
                                                         source_id          VARCHAR(64)     ,
                                                         batch_no           VARCHAR(64)     ,
                                                         account_no         TEXT            ,
                                                         scene    VARCHAR(256)     NULL     ,
                                                         account_name       TEXT            ,
                                                         bank_name          TEXT            ,
                                                         pay_type           TEXT            ,
                                                         pay_deadline       TEXT            ,
                                                         company_name       TEXT            ,
                                                         company_role       TEXT            ,
                                                         credit_code        TEXT            ,
                                                         address            TEXT            ,
                                                         company_contact_person TEXT        ,
                                                         company_contact_phone TEXT         ,
                                                         name               TEXT            ,
                                                         phone              TEXT            ,
                                                         id_card            TEXT            ,
                                                         person_role        TEXT            ,
                                                         related_subject    TEXT            ,
                                                         data_type          VARCHAR(32)     ,
                                                         create_time        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity  IS '投资协议合同 - 实体信息（信托+固有）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity OWNER TO dsjsj;
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.id                 IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.source_id          IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.scene          IS '业务类型';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.batch_no           IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.account_no         IS '银行账号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.account_name       IS '账户户名';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.bank_name          IS '开户行全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.pay_type           IS '收付款类型枚举值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.pay_deadline       IS '付款期限';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.company_name       IS '企业完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.company_role       IS '企业角色枚举值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.credit_code        IS '统一社会信用代码';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.address            IS '企业注册地址';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.company_contact_person IS '企业联系人';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.company_contact_phone  IS '企业联系方式（电话）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.name               IS '自然人姓名';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.phone              IS '联系方式';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.id_card            IS '身份证号码';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.person_role        IS '个人角色枚举值';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.related_subject    IS '关联主体完整名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.data_type          IS '数据类型(ACCOUNT/COMPANY/PERSON)';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity.create_time        IS '创建时间';

-- 建表：投资协议合同 - 关系信息
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela;
CREATE TABLE ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela (
                                                         id                 BIGSERIAL       PRIMARY KEY,
                                                         source_id          VARCHAR(64)     ,
                                                         batch_no           VARCHAR(64)     ,
                                                         scene    VARCHAR(256)     NULL,
                                                         "from"             TEXT            ,
                                                         "to"               TEXT            ,
                                                         amount             TEXT            ,
                                                         currency           TEXT            ,
                                                         ratio              TEXT            ,
                                                         remark             TEXT            ,
                                                         create_time        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela  IS '投资协议合同 - 关系信息（信托+固有）';
ALTER TABLE ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela OWNER TO dsjsj;
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela.id                 IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela.source_id          IS '源系统ID';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela.batch_no           IS '批次号';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela.scene           IS '业务类型';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela."from"             IS '流出主体全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela."to"               IS '流入主体全称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela.amount             IS '交易金额';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela.currency           IS '币种';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela.ratio              IS '股权占比';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela.remark             IS '备注/资金细分拆分';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela.create_time        IS '创建时间';


-- 百瑞信托查询语句：
-- 项目评审意见书（固有+信托）
select * from ods_dsjsj.ods_dsjsj_brxt_project_review_comment limit 100;

-- 项目重大风险信号报告单（固有+信托）
select * from ods_dsjsj.ods_dsjsj_brxt_major_risk_report limit 100;

-- 项目预警信号报告单（固有+信托）
select * from ods_dsjsj.ods_dsjsj_brxt_major_risk_warnning limit 100;

-- 项目决策委员会会议纪要（固有+信托）
select * from ods_dsjsj.ods_dsjsj_brxt_project_decision_committee limit 100;

-- 信托业务尽职调查报告（信托）
select * from ods_dsjsj.ods_dsjsj_brxt_trust_bis_due_dig_report limit 100;

-- 固有业务尽职调查报告（固有）
select * from ods_dsjsj.ods_dsjsj_brxt_own_bis_due_dig_report limit 100;

-- 保管协议（固有+信托）
select * from ods_dsjsj.ods_dsjsj_brxt_custody_agreement limit 100;

-- 信托合同/固有合同（固有+信托）
select * from ods_dsjsj.ods_dsjsj_brxt_trust_contract limit 100;

-- 借款合同（固有+信托）
select * from ods_dsjsj.ods_dsjsj_brxt_loan_contract limit 100;

-- 永续债发行文件（固有+信托）
select * from ods_dsjsj.ods_dsjsj_brxt_perpetual_bond limit 100;

-- 信托项目投后报告（信托）
select * from ods_dsjsj.ods_dsjsj_brxt_trust_post_investment_report limit 100;

-- 信托项目付款通知单（信托）
select * from ods_dsjsj.ods_dsjsj_brxt_payment_advice limit 100;

-- 信息披露报告（信托）
select * from ods_dsjsj.ods_dsjsj_brxt_information_disclosure_report limit 100;

-- 信托项目请示报告（信托）
select * from ods_dsjsj.ods_dsjsj_brxt_request_report limit 100;

-- 财务报告 - 利润表提取（固有 + 信托）
select * from ods_dsjsj.ods_dsjsj_brxt_income_statement limit 100;

-- 财务报告 - 资产负债表（固有 + 信托）
select * from ods_dsjsj.ods_dsjsj_brxt_balance_sheet limit 100;

-- 财务报告 - 现金流量表（固有 + 信托）
select * from ods_dsjsj.ods_dsjsj_brxt_cash_flow_statement limit 100;

-- 投资协议合同 - 基本信息（信托+固有）
select * from ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_info limit 100;

-- 投资协议合同 - 实体信息（信托+固有）
select * from ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_entity limit 100;

-- 投资协议合同 - 关系信息（信托+固有）
select * from ods_dsjsj.ods_dsjsj_brxt_investment_agreement_contract_rela limit 100;


-- 司库
DROP TABLE IF EXISTS ods_dsjsj.ods_dsjsj_sk_file_record;
CREATE TABLE ods_dsjsj.ods_dsjsj_sk_file_record (
      "id" varchar(255) PRIMARY KEY,
      "file_name" varchar(512),
      "file_size" bigint DEFAULT 0,
      "file_type" varchar(255),
      "file_type_name" varchar(255),
      "oss_bucket_name" varchar(128),
      "oss_file_path" varchar(1024),
      "upload_time" TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      "status" int DEFAULT 0,
      "remark" varchar(1024),
     "dataTime"        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE ods_dsjsj.ods_dsjsj_sk_file_record  IS '司库源文件下载记录表';
ALTER TABLE ods_dsjsj.ods_dsjsj_sk_file_record OWNER TO dsjsj;
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_sk_file_record.id                 IS '主键ID（自增）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_sk_file_record.file_name          IS '文件名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_sk_file_record.file_size          IS '文件大小';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_sk_file_record.file_type           IS '文件类型代码（H004、J004等）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_sk_file_record.file_type_name           IS '文件类型名称（合同、决议等）';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_sk_file_record.oss_bucket_name           IS 'OSS 桶名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_sk_file_record.oss_file_path             IS 'OSS 对象名称';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_sk_file_record.upload_time               IS '源系统文件上传时间';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_sk_file_record.status               IS '下载状态';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_sk_file_record.remark               IS '备注';
COMMENT ON COLUMN ods_dsjsj.ods_dsjsj_sk_file_record.upload_time               IS '数据接入时间';
