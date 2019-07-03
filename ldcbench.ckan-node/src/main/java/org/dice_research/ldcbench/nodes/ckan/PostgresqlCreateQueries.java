package org.dice_research.ldcbench.nodes.ckan;

public class PostgresqlCreateQueries {

    public static final String ACTIVITY_SQL = "CREATE TABLE activity (\n" +
            "    id text NOT NULL,\n" +
            "    \"timestamp\" timestamp without time zone,\n" +
            "    user_id text,\n" +
            "    object_id text,\n" +
            "    revision_id text,\n" +
            "    activity_type text,\n" +
            "    data text\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE activity ADD CONSTRAINT activity_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "\n" +
            "CREATE INDEX idx_activity_object_id ON activity USING btree (object_id, \"timestamp\");\n" +
            "CREATE INDEX idx_activity_user_id ON activity USING btree (user_id, \"timestamp\");\n" +
            "\n" +
            "ALTER TABLE activity OWNER TO ckan;";


    public static final String ACTIVITY_DETAIL =
            "CREATE TABLE activity_detail (\n" +
            "    id text NOT NULL,\n" +
            "    activity_id text,\n" +
            "    object_id text,\n" +
            "    object_type text,\n" +
            "    activity_type text,\n" +
            "    data text\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE activity_detail ADD CONSTRAINT activity_detail_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE activity_detail ADD CONSTRAINT activity_detail_activity_id_fkey\n" +
            "  FOREIGN KEY (activity_id) REFERENCES activity(id);\n" +
            "\n" +
            "CREATE INDEX idx_activity_detail_activity_id ON activity_detail USING btree (activity_id);\n" +
            "\n" +
            "ALTER TABLE activity_detail OWNER TO ckan;\n" +
            "\n" +
            "";


    public static final String AUTHORIZATION_GROUP = "CREATE TABLE authorization_group (\n" +
            "    id text NOT NULL,\n" +
            "    name text,\n" +
            "    created timestamp without time zone\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE authorization_group ADD CONSTRAINT authorization_group_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "\n" +
            "ALTER TABLE authorization_group OWNER TO ckan;";



    public static final String AUTHORIZATION_GROUP_USER = "CREATE TABLE authorization_group_user (\n" +
            "    authorization_group_id text NOT NULL,\n" +
            "    user_id text NOT NULL,\n" +
            "    id text NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE authorization_group_user ADD CONSTRAINT authorization_group_user_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE authorization_group_user ADD CONSTRAINT authorization_group_user_authorization_group_id_fkey\n" +
            "  FOREIGN KEY (authorization_group_id) REFERENCES authorization_group(id);\n" +
            "ALTER TABLE authorization_group_user ADD CONSTRAINT authorization_group_user_user_id_fkey\n" +
            "  FOREIGN KEY (user_id) REFERENCES \"user\"(id);\n" +
            "\n" +
            "ALTER TABLE authorization_group_user OWNER TO ckan;";

    public static final String DASHBOARD = "CREATE TABLE dashboard (\n" +
            "    user_id text NOT NULL,\n" +
            "    activity_stream_last_viewed timestamp without time zone NOT NULL,\n" +
            "    email_last_sent timestamp without time zone NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE public.dashboard ALTER email_last_sent SET DEFAULT ('now'::text)::timestamp without time zone;\n" +
            "\n" +
            "ALTER TABLE dashboard ADD CONSTRAINT dashboard_pkey\n" +
            "  PRIMARY KEY (user_id);\n" +
            "ALTER TABLE dashboard ADD CONSTRAINT dashboard_user_id_fkey\n" +
            "  FOREIGN KEY (user_id) REFERENCES \"user\"(id) ON UPDATE CASCADE ON DELETE CASCADE;\n" +
            "\n" +
            "ALTER TABLE dashboard OWNER TO ckan;";

    public static final String GROUP = "CREATE TABLE \"group\" (\n" +
            "    id text NOT NULL,\n" +
            "    name text NOT NULL,\n" +
            "    title text,\n" +
            "    description text,\n" +
            "    created timestamp without time zone,\n" +
            "    state text,\n" +
            "    revision_id text,\n" +
            "    type text NOT NULL,\n" +
            "    approval_status text,\n" +
            "    image_url text,\n" +
            "    is_organization boolean\n" +
            ");";

    public static final String GROUP_EXTRA = "CREATE TABLE group_extra (\n" +
            "    id text NOT NULL,\n" +
            "    group_id text,\n" +
            "    key text,\n" +
            "    value text,\n" +
            "    state text,\n" +
            "    revision_id text\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE group_extra ADD CONSTRAINT group_extra_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE group_extra ADD CONSTRAINT group_extra_group_id_fkey\n" +
            "  FOREIGN KEY (group_id) REFERENCES \"group\"(id);\n" +
            "ALTER TABLE group_extra ADD CONSTRAINT group_extra_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "\n" +
            "ALTER TABLE group_extra OWNER TO ckan;";

    public static final String GROUP_EXTRA_REVISION = "CREATE TABLE group_extra_revision (\n" +
            "    id text NOT NULL,\n" +
            "    group_id text,\n" +
            "    key text,\n" +
            "    value text,\n" +
            "    state text,\n" +
            "    revision_id text NOT NULL,\n" +
            "    continuity_id text,\n" +
            "    expired_id text,\n" +
            "    revision_timestamp timestamp without time zone,\n" +
            "    expired_timestamp timestamp without time zone,\n" +
            "    current boolean\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE group_extra_revision ADD CONSTRAINT group_extra_revision_pkey\n" +
            "  PRIMARY KEY (id, revision_id);\n" +
            "ALTER TABLE group_extra_revision ADD CONSTRAINT group_extra_revision_group_id_fkey\n" +
            "  FOREIGN KEY (group_id) REFERENCES \"group\"(id);\n" +
            "ALTER TABLE group_extra_revision ADD CONSTRAINT group_extra_revision_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "ALTER TABLE group_extra_revision ADD CONSTRAINT group_extra_revision_continuity_id_fkey\n" +
            "  FOREIGN KEY (continuity_id) REFERENCES group_extra(id);\n" +
            "\n" +
            "CREATE INDEX idx_group_extra_current ON group_extra_revision USING btree (current);\n" +
            "CREATE INDEX idx_group_extra_period ON group_extra_revision USING btree (revision_timestamp, expired_timestamp, id);\n" +
            "CREATE INDEX idx_group_extra_period_group ON group_extra_revision USING btree (revision_timestamp, expired_timestamp, group_id);\n" +
            "\n" +
            "ALTER TABLE group_extra_revision OWNER TO ckan;";

    public static final String GROUP_REVISION = "CREATE TABLE group_revision (\n" +
            "    id text NOT NULL,\n" +
            "    name text NOT NULL,\n" +
            "    title text,\n" +
            "    description text,\n" +
            "    created timestamp without time zone,\n" +
            "    state text,\n" +
            "    revision_id text NOT NULL,\n" +
            "    continuity_id text,\n" +
            "    expired_id text,\n" +
            "    revision_timestamp timestamp without time zone,\n" +
            "    expired_timestamp timestamp without time zone,\n" +
            "    current boolean,\n" +
            "    type text NOT NULL,\n" +
            "    approval_status text,\n" +
            "    image_url text,\n" +
            "    is_organization boolean\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE public.group_revision ALTER is_organization SET DEFAULT false;\n" +
            "\n" +
            "ALTER TABLE group_revision ADD CONSTRAINT group_revision_pkey\n" +
            "  PRIMARY KEY (id, revision_id);\n" +
            "ALTER TABLE group_revision ADD CONSTRAINT group_revision_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "ALTER TABLE group_revision ADD CONSTRAINT group_revision_continuity_id_fkey\n" +
            "  FOREIGN KEY (continuity_id) REFERENCES \"group\"(id);\n" +
            "\n" +
            "CREATE INDEX idx_group_current ON group_revision USING btree (current);\n" +
            "CREATE INDEX idx_group_period ON group_revision USING btree (revision_timestamp, expired_timestamp, id);\n" +
            "\n" +
            "ALTER TABLE group_revision OWNER TO ckan;";

    public static final String HARVEST_GATHER_ERROR = "CREATE TABLE harvest_gather_error (\n" +
            "    id text NOT NULL,\n" +
            "    harvest_job_id text,\n" +
            "    message text,\n" +
            "    created timestamp without time zone\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE harvest_gather_error ADD CONSTRAINT harvest_gather_error_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE harvest_gather_error ADD CONSTRAINT harvest_gather_error_harvest_job_id_fkey\n" +
            "  FOREIGN KEY (harvest_job_id) REFERENCES harvest_job(id);\n" +
            "\n" +
            "ALTER TABLE harvest_gather_error OWNER TO ckan;";

    public static final String HARVEST_JOB = "CREATE TABLE harvest_job (\n" +
            "    id text NOT NULL,\n" +
            "    created timestamp without time zone,\n" +
            "    gather_started timestamp without time zone,\n" +
            "    gather_finished timestamp without time zone,\n" +
            "    finished timestamp without time zone,\n" +
            "    source_id text,\n" +
            "    status text NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE harvest_job ADD CONSTRAINT harvest_job_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE harvest_job ADD CONSTRAINT harvest_job_source_id_fkey\n" +
            "  FOREIGN KEY (source_id) REFERENCES harvest_source(id);\n" +
            "\n" +
            "ALTER TABLE harvest_job OWNER TO ckan;";

    public static final String HARVEST_LOG = "CREATE TABLE harvest_log (\n" +
            "    id text NOT NULL,\n" +
            "    content text NOT NULL,\n" +
            "    level log_level,\n" +
            "    created timestamp without time zone\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE harvest_log ADD CONSTRAINT harvest_log_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "\n" +
            "ALTER TABLE harvest_log OWNER TO ckan;";

    public static final String HARVEST_OBJECT = "CREATE TABLE harvest_object (\n" +
            "    id text NOT NULL,\n" +
            "    guid text,\n" +
            "    current boolean,\n" +
            "    gathered timestamp without time zone,\n" +
            "    fetch_started timestamp without time zone,\n" +
            "    content text,\n" +
            "    fetch_finished timestamp without time zone,\n" +
            "    import_started timestamp without time zone,\n" +
            "    import_finished timestamp without time zone,\n" +
            "    state text,\n" +
            "    metadata_modified_date timestamp without time zone,\n" +
            "    retry_times integer,\n" +
            "    harvest_job_id text,\n" +
            "    harvest_source_id text,\n" +
            "    package_id text,\n" +
            "    report_status text\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE harvest_object ADD CONSTRAINT harvest_object_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE harvest_object ADD CONSTRAINT harvest_object_harvest_job_id_fkey\n" +
            "  FOREIGN KEY (harvest_job_id) REFERENCES harvest_job(id);\n" +
            "ALTER TABLE harvest_object ADD CONSTRAINT harvest_object_harvest_source_id_fkey\n" +
            "  FOREIGN KEY (harvest_source_id) REFERENCES harvest_source(id);\n" +
            "ALTER TABLE harvest_object ADD CONSTRAINT harvest_object_package_id_fkey\n" +
            "  FOREIGN KEY (package_id) REFERENCES package(id) DEFERRABLE;\n" +
            "\n" +
            "CREATE INDEX harvest_job_id_idx ON harvest_object USING btree (harvest_job_id);\n" +
            "\n" +
            "ALTER TABLE harvest_object OWNER TO ckan;";

    public static final String HARVEST_OBJECT_ERROR = "CREATE TABLE harvest_object_error (\n" +
            "    id text NOT NULL,\n" +
            "    harvest_object_id text,\n" +
            "    message text,\n" +
            "    stage text,\n" +
            "    line integer,\n" +
            "    created timestamp without time zone\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE harvest_object_error ADD CONSTRAINT harvest_object_error_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE harvest_object_error ADD CONSTRAINT harvest_object_error_harvest_object_id_fkey\n" +
            "  FOREIGN KEY (harvest_object_id) REFERENCES harvest_object(id);\n" +
            "\n" +
            "ALTER TABLE harvest_object_error OWNER TO ckan;";

    public static final String HARVEST_OBJECT_EXTRA = "CREATE TABLE harvest_object_extra (\n" +
            "    id text NOT NULL,\n" +
            "    harvest_object_id text,\n" +
            "    key text,\n" +
            "    value text\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE harvest_object_extra ADD CONSTRAINT harvest_object_extra_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE harvest_object_extra ADD CONSTRAINT harvest_object_extra_harvest_object_id_fkey\n" +
            "  FOREIGN KEY (harvest_object_id) REFERENCES harvest_object(id);\n" +
            "\n" +
            "ALTER TABLE harvest_object_extra OWNER TO ckan;";

    public static final String HARVEST_SOURCE = "CREATE TABLE harvest_source (\n" +
            "    id text NOT NULL,\n" +
            "    url text NOT NULL,\n" +
            "    title text,\n" +
            "    description text,\n" +
            "    config text,\n" +
            "    created timestamp without time zone,\n" +
            "    type text NOT NULL,\n" +
            "    active boolean,\n" +
            "    user_id text,\n" +
            "    publisher_id text,\n" +
            "    frequency text,\n" +
            "    next_run timestamp without time zone\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE harvest_source ADD CONSTRAINT harvest_source_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "\n" +
            "ALTER TABLE harvest_source OWNER TO ckan;";

    public static final String MEMBER = "CREATE TABLE member (\n" +
            "    id text NOT NULL,\n" +
            "    table_id text NOT NULL,\n" +
            "    group_id text,\n" +
            "    state text,\n" +
            "    revision_id text,\n" +
            "    table_name text NOT NULL,\n" +
            "    capacity text NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE member ADD CONSTRAINT member_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE member ADD CONSTRAINT member_group_id_fkey\n" +
            "  FOREIGN KEY (group_id) REFERENCES \"group\"(id);\n" +
            "ALTER TABLE member ADD CONSTRAINT member_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "\n" +
            "CREATE INDEX idx_extra_grp_id_pkg_id ON member USING btree (group_id, table_id);\n" +
            "CREATE INDEX idx_group_pkg_id ON member USING btree (table_id);\n" +
            "CREATE INDEX idx_package_group_group_id ON member USING btree (group_id);\n" +
            "CREATE INDEX idx_package_group_id ON member USING btree (id);\n" +
            "CREATE INDEX idx_package_group_pkg_id ON member USING btree (table_id);\n" +
            "CREATE INDEX idx_package_group_pkg_id_group_id ON member USING btree (group_id, table_id);\n" +
            "\n" +
            "ALTER TABLE member OWNER TO ckan;";

    public static final String MEMBER_REVISION = "CREATE TABLE member_revision (\n" +
            "    id text NOT NULL,\n" +
            "    table_id text NOT NULL,\n" +
            "    group_id text,\n" +
            "    state text,\n" +
            "    revision_id text NOT NULL,\n" +
            "    continuity_id text,\n" +
            "    expired_id text,\n" +
            "    revision_timestamp timestamp without time zone,\n" +
            "    expired_timestamp timestamp without time zone,\n" +
            "    current boolean,\n" +
            "    table_name text NOT NULL,\n" +
            "    capacity text NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE member_revision ADD CONSTRAINT member_revision_pkey\n" +
            "  PRIMARY KEY (id, revision_id);\n" +
            "ALTER TABLE member_revision ADD CONSTRAINT member_revision_continuity_id_fkey\n" +
            "  FOREIGN KEY (continuity_id) REFERENCES member(id);\n" +
            "ALTER TABLE member_revision ADD CONSTRAINT member_revision_group_id_fkey\n" +
            "  FOREIGN KEY (group_id) REFERENCES \"group\"(id);\n" +
            "ALTER TABLE member_revision ADD CONSTRAINT member_revision_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "\n" +
            "CREATE INDEX idx_member_continuity_id ON member_revision USING btree (continuity_id);\n" +
            "CREATE INDEX idx_package_group_current ON member_revision USING btree (current);\n" +
            "CREATE INDEX idx_package_group_period_package_group ON member_revision USING btree (revision_timestamp, expired_timestamp, table_id, group_id);\n" +
            "\n" +
            "ALTER TABLE member_revision OWNER TO ckan;";

    public static final String MIGRATE_VERSION = "CREATE TABLE migrate_version (\n" +
            "    repository_id character varying(250) NOT NULL,\n" +
            "    repository_path text,\n" +
            "    version integer\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE migrate_version ADD CONSTRAINT migrate_version_pkey\n" +
            "  PRIMARY KEY (repository_id);\n" +
            "\n" +
            "ALTER TABLE migrate_version OWNER TO ckan;\n" +
            "";

    public static final String PACKAGE = "CREATE TABLE package (\n" +
            "    id text NOT NULL,\n" +
            "    name character varying(100) NOT NULL,\n" +
            "    title text,\n" +
            "    version character varying(100),\n" +
            "    url text,\n" +
            "    notes text,\n" +
            "    license_id text,\n" +
            "    revision_id text,\n" +
            "    author text,\n" +
            "    author_email text,\n" +
            "    maintainer text,\n" +
            "    maintainer_email text,\n" +
            "    state text,\n" +
            "    type text,\n" +
            "    owner_org text,\n" +
            "    private boolean,\n" +
            "    metadata_modified timestamp without time zone,\n" +
            "    creator_user_id text,\n" +
            "    metadata_created timestamp without time zone\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE public.package ALTER private SET DEFAULT false;\n" +
            "\n" +
            "ALTER TABLE package ADD CONSTRAINT package_name_key\n" +
            "  UNIQUE (name);\n" +
            "ALTER TABLE package ADD CONSTRAINT package_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE package ADD CONSTRAINT package_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "\n" +
            "CREATE INDEX idx_package_creator_user_id ON package USING btree (creator_user_id);\n" +
            "CREATE INDEX idx_pkg_id ON package USING btree (id);\n" +
            "CREATE INDEX idx_pkg_lname ON package USING btree (lower((name)::text));\n" +
            "CREATE INDEX idx_pkg_name ON package USING btree (name);\n" +
            "CREATE INDEX idx_pkg_rev_id ON package USING btree (revision_id);\n" +
            "CREATE INDEX idx_pkg_sid ON package USING btree (id, state);\n" +
            "CREATE INDEX idx_pkg_slname ON package USING btree (lower((name)::text), state);\n" +
            "CREATE INDEX idx_pkg_sname ON package USING btree (name, state);\n" +
            "CREATE INDEX idx_pkg_srev_id ON package USING btree (revision_id, state);\n" +
            "CREATE INDEX idx_pkg_stitle ON package USING btree (title, state);\n" +
            "CREATE INDEX idx_pkg_suname ON package USING btree (upper((name)::text), state);\n" +
            "CREATE INDEX idx_pkg_title ON package USING btree (title);\n" +
            "CREATE INDEX idx_pkg_uname ON package USING btree (upper((name)::text));\n" +
            "\n" +
            "ALTER TABLE package OWNER TO ckan;";

    public static final String PACKAGE_EXTRA = "CREATE TABLE package_extra (\n" +
            "    id text NOT NULL,\n" +
            "    package_id text,\n" +
            "    key text,\n" +
            "    value text,\n" +
            "    revision_id text,\n" +
            "    state text\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE package_extra ADD CONSTRAINT package_extra_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE package_extra ADD CONSTRAINT package_extra_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "ALTER TABLE package_extra ADD CONSTRAINT package_extra_package_id_fkey\n" +
            "  FOREIGN KEY (package_id) REFERENCES package(id);\n" +
            "\n" +
            "CREATE INDEX idx_extra_id_pkg_id ON package_extra USING btree (id, package_id);\n" +
            "CREATE INDEX idx_extra_pkg_id ON package_extra USING btree (package_id);\n" +
            "\n" +
            "ALTER TABLE package_extra OWNER TO ckan;\n" +
            "";

    public static final String PACKAGE_EXTRA_REVISION = "CREATE TABLE package_extra_revision (\n" +
            "    id text NOT NULL,\n" +
            "    package_id text,\n" +
            "    key text,\n" +
            "    value text,\n" +
            "    revision_id text NOT NULL,\n" +
            "    continuity_id text,\n" +
            "    state text,\n" +
            "    expired_id text,\n" +
            "    revision_timestamp timestamp without time zone,\n" +
            "    expired_timestamp timestamp without time zone,\n" +
            "    current boolean\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE package_extra_revision ADD CONSTRAINT package_extra_revision_pkey\n" +
            "  PRIMARY KEY (id, revision_id);\n" +
            "ALTER TABLE package_extra_revision ADD CONSTRAINT package_extra_revision_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "ALTER TABLE package_extra_revision ADD CONSTRAINT package_extra_revision_package_id_fkey\n" +
            "  FOREIGN KEY (package_id) REFERENCES package(id);\n" +
            "ALTER TABLE package_extra_revision ADD CONSTRAINT package_extra_revision_continuity_id_fkey\n" +
            "  FOREIGN KEY (continuity_id) REFERENCES package_extra(id);\n" +
            "\n" +
            "CREATE INDEX idx_package_extra_continuity_id ON package_extra_revision USING btree (continuity_id);\n" +
            "CREATE INDEX idx_package_extra_current ON package_extra_revision USING btree (current);\n" +
            "CREATE INDEX idx_package_extra_package_id ON package_extra_revision USING btree (package_id, current);\n" +
            "CREATE INDEX idx_package_extra_period ON package_extra_revision USING btree (revision_timestamp, expired_timestamp, id);\n" +
            "CREATE INDEX idx_package_extra_period_package ON package_extra_revision USING btree (revision_timestamp, expired_timestamp, package_id);\n" +
            "CREATE INDEX idx_package_extra_rev_id ON package_extra_revision USING btree (revision_id);\n" +
            "\n" +
            "ALTER TABLE package_extra_revision OWNER TO ckan;";

    public static final String PACKAGE_RELATIONSHIP = "CREATE TABLE package_relationship (\n" +
            "    id text NOT NULL,\n" +
            "    subject_package_id text,\n" +
            "    object_package_id text,\n" +
            "    type text,\n" +
            "    comment text,\n" +
            "    revision_id text,\n" +
            "    state text\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE package_relationship ADD CONSTRAINT package_relationship_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE package_relationship ADD CONSTRAINT package_relationship_subject_package_id_fkey\n" +
            "  FOREIGN KEY (subject_package_id) REFERENCES package(id);\n" +
            "ALTER TABLE package_relationship ADD CONSTRAINT package_relationship_object_package_id_fkey\n" +
            "  FOREIGN KEY (object_package_id) REFERENCES package(id);\n" +
            "ALTER TABLE package_relationship ADD CONSTRAINT package_relationship_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "\n" +
            "ALTER TABLE package_relationship OWNER TO ckan;";

    public static final String PACKAGE_RELATIONSHIP_REVISION = "CREATE TABLE package_relationship_revision (\n" +
            "    id text NOT NULL,\n" +
            "    subject_package_id text,\n" +
            "    object_package_id text,\n" +
            "    type text,\n" +
            "    comment text,\n" +
            "    revision_id text NOT NULL,\n" +
            "    continuity_id text,\n" +
            "    state text,\n" +
            "    expired_id text,\n" +
            "    revision_timestamp timestamp without time zone,\n" +
            "    expired_timestamp timestamp without time zone,\n" +
            "    current boolean\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE package_relationship_revision ADD CONSTRAINT package_relationship_revision_pkey\n" +
            "  PRIMARY KEY (id, revision_id);\n" +
            "ALTER TABLE package_relationship_revision ADD CONSTRAINT package_relationship_revision_subject_package_id_fkey\n" +
            "  FOREIGN KEY (subject_package_id) REFERENCES package(id);\n" +
            "ALTER TABLE package_relationship_revision ADD CONSTRAINT package_relationship_revision_object_package_id_fkey\n" +
            "  FOREIGN KEY (object_package_id) REFERENCES package(id);\n" +
            "ALTER TABLE package_relationship_revision ADD CONSTRAINT package_relationship_revision_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "ALTER TABLE package_relationship_revision ADD CONSTRAINT package_relationship_revision_continuity_id_fkey\n" +
            "  FOREIGN KEY (continuity_id) REFERENCES package_relationship(id);\n" +
            "\n" +
            "CREATE INDEX idx_package_relationship_current ON package_relationship_revision USING btree (current);\n" +
            "CREATE INDEX idx_period_package_relationship ON package_relationship_revision USING btree (revision_timestamp, expired_timestamp, object_package_id, subject_package_id);\n" +
            "\n" +
            "ALTER TABLE package_relationship_revision OWNER TO ckan;";

    public static final String PACKAGE_REVISION = "CREATE TABLE package_revision (\n" +
            "    id text NOT NULL,\n" +
            "    name character varying(100) NOT NULL,\n" +
            "    title text,\n" +
            "    version character varying(100),\n" +
            "    url text,\n" +
            "    notes text,\n" +
            "    license_id text,\n" +
            "    revision_id text NOT NULL,\n" +
            "    continuity_id text,\n" +
            "    author text,\n" +
            "    author_email text,\n" +
            "    maintainer text,\n" +
            "    maintainer_email text,\n" +
            "    state text,\n" +
            "    expired_id text,\n" +
            "    revision_timestamp timestamp without time zone,\n" +
            "    expired_timestamp timestamp without time zone,\n" +
            "    current boolean,\n" +
            "    type text,\n" +
            "    owner_org text,\n" +
            "    private boolean,\n" +
            "    metadata_modified timestamp without time zone,\n" +
            "    creator_user_id text,\n" +
            "    metadata_created timestamp without time zone\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE public.package_revision ALTER private SET DEFAULT false;\n" +
            "\n" +
            "ALTER TABLE package_revision ADD CONSTRAINT package_revision_pkey\n" +
            "  PRIMARY KEY (id, revision_id);\n" +
            "ALTER TABLE package_revision ADD CONSTRAINT package_revision_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "ALTER TABLE package_revision ADD CONSTRAINT package_revision_continuity_id_fkey\n" +
            "  FOREIGN KEY (continuity_id) REFERENCES package(id);\n" +
            "\n" +
            "CREATE INDEX idx_package_continuity_id ON package_revision USING btree (continuity_id);\n" +
            "CREATE INDEX idx_package_current ON package_revision USING btree (current);\n" +
            "CREATE INDEX idx_package_period ON package_revision USING btree (revision_timestamp, expired_timestamp, id);\n" +
            "CREATE INDEX idx_pkg_revision_id ON package_revision USING btree (id);\n" +
            "CREATE INDEX idx_pkg_revision_name ON package_revision USING btree (name);\n" +
            "CREATE INDEX idx_pkg_revision_rev_id ON package_revision USING btree (revision_id);\n" +
            "\n" +
            "ALTER TABLE package_revision OWNER TO ckan;";

    public static final String PACKAGE_TAG = "CREATE TABLE package_tag (\n" +
            "    id text NOT NULL,\n" +
            "    package_id text,\n" +
            "    tag_id text,\n" +
            "    revision_id text,\n" +
            "    state text\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE package_tag ADD CONSTRAINT package_tag_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE package_tag ADD CONSTRAINT package_tag_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "ALTER TABLE package_tag ADD CONSTRAINT package_tag_package_id_fkey\n" +
            "  FOREIGN KEY (package_id) REFERENCES package(id);\n" +
            "ALTER TABLE package_tag ADD CONSTRAINT package_tag_tag_id_fkey\n" +
            "  FOREIGN KEY (tag_id) REFERENCES tag(id);\n" +
            "\n" +
            "CREATE INDEX idx_package_tag_id ON package_tag USING btree (id);\n" +
            "CREATE INDEX idx_package_tag_pkg_id ON package_tag USING btree (package_id);\n" +
            "CREATE INDEX idx_package_tag_pkg_id_tag_id ON package_tag USING btree (tag_id, package_id);\n" +
            "CREATE INDEX idx_package_tag_tag_id ON package_tag USING btree (tag_id);\n" +
            "\n" +
            "ALTER TABLE package_tag OWNER TO ckan;";

    public static final String PACKAGE_TAG_REVISION = "CREATE TABLE package_tag_revision (\n" +
            "    id text NOT NULL,\n" +
            "    package_id text,\n" +
            "    tag_id text,\n" +
            "    revision_id text NOT NULL,\n" +
            "    continuity_id text,\n" +
            "    state text,\n" +
            "    expired_id text,\n" +
            "    revision_timestamp timestamp without time zone,\n" +
            "    expired_timestamp timestamp without time zone,\n" +
            "    current boolean\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE package_tag_revision ADD CONSTRAINT package_tag_revision_pkey\n" +
            "  PRIMARY KEY (id, revision_id);\n" +
            "ALTER TABLE package_tag_revision ADD CONSTRAINT package_tag_revision_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "ALTER TABLE package_tag_revision ADD CONSTRAINT package_tag_revision_package_id_fkey\n" +
            "  FOREIGN KEY (package_id) REFERENCES package(id);\n" +
            "ALTER TABLE package_tag_revision ADD CONSTRAINT package_tag_revision_continuity_id_fkey\n" +
            "  FOREIGN KEY (continuity_id) REFERENCES package_tag(id);\n" +
            "ALTER TABLE package_tag_revision ADD CONSTRAINT package_tag_revision_tag_id_fkey\n" +
            "  FOREIGN KEY (tag_id) REFERENCES tag(id);\n" +
            "\n" +
            "CREATE INDEX idx_package_tag_continuity_id ON package_tag_revision USING btree (continuity_id);\n" +
            "CREATE INDEX idx_package_tag_current ON package_tag_revision USING btree (current);\n" +
            "CREATE INDEX idx_package_tag_revision_id ON package_tag_revision USING btree (id);\n" +
            "CREATE INDEX idx_package_tag_revision_pkg_id ON package_tag_revision USING btree (package_id);\n" +
            "CREATE INDEX idx_package_tag_revision_pkg_id_tag_id ON package_tag_revision USING btree (tag_id, package_id);\n" +
            "CREATE INDEX idx_package_tag_revision_rev_id ON package_tag_revision USING btree (revision_id);\n" +
            "CREATE INDEX idx_package_tag_revision_tag_id ON package_tag_revision USING btree (tag_id);\n" +
            "CREATE INDEX idx_period_package_tag ON package_tag_revision USING btree (revision_timestamp, expired_timestamp, package_id, tag_id);\n" +
            "\n" +
            "ALTER TABLE package_tag_revision OWNER TO ckan;";

    public static final String RATING = "CREATE TABLE rating (\n" +
            "    id text NOT NULL,\n" +
            "    user_id text,\n" +
            "    user_ip_address text,\n" +
            "    package_id text,\n" +
            "    rating double precision,\n" +
            "    created timestamp without time zone\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE rating ADD CONSTRAINT rating_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE rating ADD CONSTRAINT rating_user_id_fkey\n" +
            "  FOREIGN KEY (user_id) REFERENCES \"user\"(id);\n" +
            "ALTER TABLE rating ADD CONSTRAINT rating_package_id_fkey\n" +
            "  FOREIGN KEY (package_id) REFERENCES package(id);\n" +
            "\n" +
            "CREATE INDEX idx_rating_id ON rating USING btree (id);\n" +
            "CREATE INDEX idx_rating_package_id ON rating USING btree (package_id);\n" +
            "CREATE INDEX idx_rating_user_id ON rating USING btree (user_id);\n" +
            "\n" +
            "ALTER TABLE rating OWNER TO ckan;";

    public static final String RESOURCE = "CREATE TABLE resource (\n" +
            "    id text NOT NULL,\n" +
            "    url text NOT NULL,\n" +
            "    format text,\n" +
            "    description text,\n" +
            "    \"position\" integer,\n" +
            "    revision_id text,\n" +
            "    hash text,\n" +
            "    state text,\n" +
            "    extras text,\n" +
            "    name text,\n" +
            "    resource_type text,\n" +
            "    mimetype text,\n" +
            "    mimetype_inner text,\n" +
            "    size bigint,\n" +
            "    last_modified timestamp without time zone,\n" +
            "    cache_url text,\n" +
            "    cache_last_updated timestamp without time zone,\n" +
            "    webstore_url text,\n" +
            "    webstore_last_updated timestamp without time zone,\n" +
            "    created timestamp without time zone,\n" +
            "    url_type text,\n" +
            "    package_id text NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE public.resource ALTER package_id SET DEFAULT ''::text;\n" +
            "\n" +
            "ALTER TABLE resource ADD CONSTRAINT resource_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE resource ADD CONSTRAINT resource_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "\n" +
            "CREATE INDEX idx_package_resource_id ON resource USING btree (id);\n" +
            "CREATE INDEX idx_package_resource_url ON resource USING btree (url);\n" +
            "\n" +
            "ALTER TABLE resource OWNER TO ckan;";

    public static final String RESOURCE_REVISION = "CREATE TABLE resource_revision (\n" +
            "    id text NOT NULL,\n" +
            "    url text NOT NULL,\n" +
            "    format text,\n" +
            "    description text,\n" +
            "    \"position\" integer,\n" +
            "    revision_id text NOT NULL,\n" +
            "    continuity_id text,\n" +
            "    hash text,\n" +
            "    state text,\n" +
            "    extras text,\n" +
            "    expired_id text,\n" +
            "    revision_timestamp timestamp without time zone,\n" +
            "    expired_timestamp timestamp without time zone,\n" +
            "    current boolean,\n" +
            "    name text,\n" +
            "    resource_type text,\n" +
            "    mimetype text,\n" +
            "    mimetype_inner text,\n" +
            "    size bigint,\n" +
            "    last_modified timestamp without time zone,\n" +
            "    cache_url text,\n" +
            "    cache_last_updated timestamp without time zone,\n" +
            "    webstore_url text,\n" +
            "    webstore_last_updated timestamp without time zone,\n" +
            "    created timestamp without time zone,\n" +
            "    url_type text,\n" +
            "    package_id text NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE public.resource_revision ALTER package_id SET DEFAULT ''::text;\n" +
            "\n" +
            "ALTER TABLE resource_revision ADD CONSTRAINT resource_revision_pkey\n" +
            "  PRIMARY KEY (id, revision_id);\n" +
            "ALTER TABLE resource_revision ADD CONSTRAINT resource_revision_continuity_id_fkey\n" +
            "  FOREIGN KEY (continuity_id) REFERENCES resource(id);\n" +
            "ALTER TABLE resource_revision ADD CONSTRAINT resource_revision_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "\n" +
            "CREATE INDEX idx_package_resource_rev_id ON resource_revision USING btree (revision_id);\n" +
            "CREATE INDEX idx_resource_continuity_id ON resource_revision USING btree (continuity_id);\n" +
            "CREATE INDEX idx_resource_current ON resource_revision USING btree (current);\n" +
            "CREATE INDEX idx_resource_period ON resource_revision USING btree (revision_timestamp, expired_timestamp, id);\n" +
            "\n" +
            "ALTER TABLE resource_revision OWNER TO ckan;";

    public static final String RESOURCE_VIEW = "CREATE TABLE resource_view (\n" +
            "    id text NOT NULL,\n" +
            "    resource_id text,\n" +
            "    title text,\n" +
            "    description text,\n" +
            "    view_type text NOT NULL,\n" +
            "    \"order\" integer NOT NULL,\n" +
            "    config text\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE resource_view ADD CONSTRAINT resource_view_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE resource_view ADD CONSTRAINT resource_view_resource_id_fkey\n" +
            "  FOREIGN KEY (resource_id) REFERENCES resource(id) ON UPDATE CASCADE ON DELETE CASCADE;\n" +
            "\n" +
            "ALTER TABLE resource_view OWNER TO ckan;\n" +
            "";

    public static final String REVISION = "CREATE TABLE revision (\n" +
            "    id text NOT NULL,\n" +
            "    \"timestamp\" timestamp without time zone,\n" +
            "    author character varying(200),\n" +
            "    message text,\n" +
            "    state text,\n" +
            "    approved_timestamp timestamp without time zone\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE revision ADD CONSTRAINT revision_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "\n" +
            "CREATE INDEX idx_revision_author ON revision USING btree (author);\n" +
            "CREATE INDEX idx_rev_state ON revision USING btree (state);\n" +
            "\n" +
            "ALTER TABLE revision OWNER TO ckan;";

    public static final String SYSTEM_INFO = "CREATE TABLE system_info (\n" +
            "    id integer NOT NULL,\n" +
            "    key character varying(100) NOT NULL,\n" +
            "    value text,\n" +
            "    revision_id text,\n" +
            "    state text NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE public.system_info ALTER id SET DEFAULT nextval('system_info_id_seq'::regclass);\n" +
            "ALTER TABLE public.system_info ALTER state SET DEFAULT 'active'::text;\n" +
            "\n" +
            "ALTER TABLE system_info ADD CONSTRAINT system_info_key_key\n" +
            "  UNIQUE (key);\n" +
            "ALTER TABLE system_info ADD CONSTRAINT system_info_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE system_info ADD CONSTRAINT system_info_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "\n" +
            "ALTER TABLE system_info OWNER TO ckan;\n" +
            "";

    public static final String SYSTEM_INFO_REVISION = "CREATE TABLE system_info_revision (\n" +
            "    id integer NOT NULL,\n" +
            "    key character varying(100) NOT NULL,\n" +
            "    value text,\n" +
            "    revision_id text NOT NULL,\n" +
            "    continuity_id integer,\n" +
            "    state text NOT NULL,\n" +
            "    expired_id text,\n" +
            "    revision_timestamp timestamp without time zone,\n" +
            "    expired_timestamp timestamp without time zone,\n" +
            "    current boolean\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE public.system_info_revision ALTER state SET DEFAULT 'active'::text;\n" +
            "\n" +
            "ALTER TABLE system_info_revision ADD CONSTRAINT system_info_revision_pkey\n" +
            "  PRIMARY KEY (id, revision_id);\n" +
            "ALTER TABLE system_info_revision ADD CONSTRAINT system_info_revision_revision_id_fkey\n" +
            "  FOREIGN KEY (revision_id) REFERENCES revision(id);\n" +
            "ALTER TABLE system_info_revision ADD CONSTRAINT system_info_revision_continuity_id_fkey\n" +
            "  FOREIGN KEY (continuity_id) REFERENCES system_info(id);\n" +
            "\n" +
            "ALTER TABLE system_info_revision OWNER TO ckan;";

    public static final String TAG = "CREATE TABLE tag (\n" +
            "    id text NOT NULL,\n" +
            "    name character varying(100) NOT NULL,\n" +
            "    vocabulary_id character varying(100)\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE tag ADD CONSTRAINT tag_name_vocabulary_id_key\n" +
            "  UNIQUE (name, vocabulary_id);\n" +
            "ALTER TABLE tag ADD CONSTRAINT tag_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "ALTER TABLE tag ADD CONSTRAINT tag_vocabulary_id_fkey\n" +
            "  FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id);\n" +
            "\n" +
            "CREATE INDEX idx_tag_id ON tag USING btree (id);\n" +
            "CREATE INDEX idx_tag_name ON tag USING btree (name);\n" +
            "\n" +
            "ALTER TABLE tag OWNER TO ckan;";

    public static final String TASK_STATUS = "CREATE TABLE task_status (\n" +
            "    id text NOT NULL,\n" +
            "    entity_id text NOT NULL,\n" +
            "    entity_type text NOT NULL,\n" +
            "    task_type text NOT NULL,\n" +
            "    key text NOT NULL,\n" +
            "    value text NOT NULL,\n" +
            "    state text,\n" +
            "    error text,\n" +
            "    last_updated timestamp without time zone\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE task_status ADD CONSTRAINT task_status_entity_id_task_type_key_key\n" +
            "  UNIQUE (entity_id, task_type, key);\n" +
            "ALTER TABLE task_status ADD CONSTRAINT task_status_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "\n" +
            "ALTER TABLE task_status OWNER TO ckan;";

    public static final String TERM_TRANSLATION = "CREATE TABLE term_translation (\n" +
            "    term text NOT NULL,\n" +
            "    term_translation text NOT NULL,\n" +
            "    lang_code text NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "CREATE INDEX term ON term_translation USING btree (term);\n" +
            "CREATE INDEX term_lang ON term_translation USING btree (term, lang_code);\n" +
            "\n" +
            "ALTER TABLE term_translation OWNER TO ckan;";

    public static final String TRACKING_RAW = "CREATE TABLE tracking_raw (\n" +
            "    user_key character varying(100) NOT NULL,\n" +
            "    url text NOT NULL,\n" +
            "    tracking_type character varying(10) NOT NULL,\n" +
            "    access_timestamp timestamp without time zone\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE public.tracking_raw ALTER access_timestamp SET DEFAULT now();\n" +
            "\n" +
            "CREATE INDEX tracking_raw_access_timestamp ON tracking_raw USING btree (access_timestamp);\n" +
            "CREATE INDEX tracking_raw_url ON tracking_raw USING btree (url);\n" +
            "CREATE INDEX tracking_raw_user_key ON tracking_raw USING btree (user_key);\n" +
            "\n" +
            "ALTER TABLE tracking_raw OWNER TO ckan;";

    public static final String TRACKING_SUMMARY = "CREATE TABLE tracking_summary (\n" +
            "    url text NOT NULL,\n" +
            "    package_id text,\n" +
            "    tracking_type character varying(10) NOT NULL,\n" +
            "    count integer NOT NULL,\n" +
            "    running_total integer NOT NULL,\n" +
            "    recent_views integer NOT NULL,\n" +
            "    tracking_date date\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE public.tracking_summary ALTER running_total SET DEFAULT 0;\n" +
            "ALTER TABLE public.tracking_summary ALTER recent_views SET DEFAULT 0;\n" +
            "\n" +
            "CREATE INDEX tracking_summary_date ON tracking_summary USING btree (tracking_date);\n" +
            "CREATE INDEX tracking_summary_package_id ON tracking_summary USING btree (package_id);\n" +
            "CREATE INDEX tracking_summary_url ON tracking_summary USING btree (url);\n" +
            "\n" +
            "ALTER TABLE tracking_summary OWNER TO ckan;";

    public static final String USER = "CREATE TABLE \"user\" (\n" +
            "    id text NOT NULL,\n" +
            "    name text NOT NULL,\n" +
            "    apikey text,\n" +
            "    created timestamp without time zone,\n" +
            "    about text,\n" +
            "    password text,\n" +
            "    fullname text,\n" +
            "    email text,\n" +
            "    reset_key text,\n" +
            "    sysadmin boolean,\n" +
            "    activity_streams_email_notifications boolean,\n" +
            "    state text NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE public.\"user\" ALTER sysadmin SET DEFAULT false;\n" +
            "ALTER TABLE public.\"user\" ALTER activity_streams_email_notifications SET DEFAULT false;\n" +
            "ALTER TABLE public.\"user\" ALTER state SET DEFAULT 'active'::text;\n" +
            "\n" +
            "ALTER TABLE \"user\" ADD CONSTRAINT user_name_key\n" +
            "  UNIQUE (name);\n" +
            "ALTER TABLE \"user\" ADD CONSTRAINT user_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "\n" +
            "CREATE INDEX idx_user_id ON \"user\" USING btree (id);\n" +
            "CREATE INDEX idx_user_name ON \"user\" USING btree (name);\n" +
            "CREATE INDEX idx_user_name_index ON \"user\" USING btree ((\n" +
            "CASE\n" +
            "    WHEN ((fullname IS NULL) OR (fullname = ''::text)) THEN name\n" +
            "    ELSE fullname\n" +
            "END));\n" +
            "\n" +
            "ALTER TABLE \"user\" OWNER TO ckan;\n" +
            "";

    public static final String USER_FOLLOWING_DATASET= "CREATE TABLE user_following_dataset (\n" +
            "    follower_id text NOT NULL,\n" +
            "    object_id text NOT NULL,\n" +
            "    datetime timestamp without time zone NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE user_following_dataset ADD CONSTRAINT user_following_dataset_pkey\n" +
            "  PRIMARY KEY (follower_id, object_id);\n" +
            "ALTER TABLE user_following_dataset ADD CONSTRAINT user_following_dataset_follower_id_fkey\n" +
            "  FOREIGN KEY (follower_id) REFERENCES \"user\"(id) ON UPDATE CASCADE ON DELETE CASCADE;\n" +
            "ALTER TABLE user_following_dataset ADD CONSTRAINT user_following_dataset_object_id_fkey\n" +
            "  FOREIGN KEY (object_id) REFERENCES package(id) ON UPDATE CASCADE ON DELETE CASCADE;\n" +
            "\n" +
            "ALTER TABLE user_following_dataset OWNER TO ckan;\n" +
            "";

    public static final String USER_FOLLOWING_GROUP = "CREATE TABLE user_following_group (\n" +
            "    follower_id text NOT NULL,\n" +
            "    object_id text NOT NULL,\n" +
            "    datetime timestamp without time zone NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE user_following_group ADD CONSTRAINT user_following_group_pkey\n" +
            "  PRIMARY KEY (follower_id, object_id);\n" +
            "ALTER TABLE user_following_group ADD CONSTRAINT user_following_group_user_id_fkey\n" +
            "  FOREIGN KEY (follower_id) REFERENCES \"user\"(id) ON UPDATE CASCADE ON DELETE CASCADE;\n" +
            "ALTER TABLE user_following_group ADD CONSTRAINT user_following_group_group_id_fkey\n" +
            "  FOREIGN KEY (object_id) REFERENCES \"group\"(id) ON UPDATE CASCADE ON DELETE CASCADE;\n" +
            "\n" +
            "ALTER TABLE user_following_group OWNER TO ckan;";


    public static final String USER_FOLLOWING_USER = "CREATE TABLE user_following_user (\n" +
            "    follower_id text NOT NULL,\n" +
            "    object_id text NOT NULL,\n" +
            "    datetime timestamp without time zone NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE user_following_user ADD CONSTRAINT user_following_user_pkey\n" +
            "  PRIMARY KEY (follower_id, object_id);\n" +
            "ALTER TABLE user_following_user ADD CONSTRAINT user_following_user_follower_id_fkey\n" +
            "  FOREIGN KEY (follower_id) REFERENCES \"user\"(id) ON UPDATE CASCADE ON DELETE CASCADE;\n" +
            "ALTER TABLE user_following_user ADD CONSTRAINT user_following_user_object_id_fkey\n" +
            "  FOREIGN KEY (object_id) REFERENCES \"user\"(id) ON UPDATE CASCADE ON DELETE CASCADE;\n" +
            "\n" +
            "ALTER TABLE user_following_user OWNER TO ckan;";

    public static final String VOCABULARY = "CREATE TABLE vocabulary (\n" +
            "    id text NOT NULL,\n" +
            "    name character varying(100) NOT NULL\n" +
            ");\n" +
            "\n" +
            "\n" +
            "ALTER TABLE vocabulary ADD CONSTRAINT vocabulary_name_key\n" +
            "  UNIQUE (name);\n" +
            "ALTER TABLE vocabulary ADD CONSTRAINT vocabulary_pkey\n" +
            "  PRIMARY KEY (id);\n" +
            "\n" +
            "ALTER TABLE vocabulary OWNER TO ckan;";




}
