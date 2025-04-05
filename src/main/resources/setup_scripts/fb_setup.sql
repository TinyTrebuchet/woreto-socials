CREATE TABLE IF NOT EXISTS fb_account (
    emailId TEXT PRIMARY KEY,
    password TEXT NOT NULL,
    fullName TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS fb_page (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    keywords TEXT,
    lastPosted INTEGER NOT NULL,
    managerId TEXT NOT NULL,
    groupsToShare TEXT NOT NULL,
    FOREIGN KEY (managerId) REFERENCES fb_account (emailId)
);

CREATE TABLE IF NOT EXISTS fb_group (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    managerId TEXT NOT NULL,
    lastPosted TEXT NOT NULL,
    FOREIGN KEY (managerId) REFERENCES managerId (emailId)
);

CREATE TABLE IF NOT EXISTS fb_page_post (
    storyId TEXT PRIMARY KEY,
    actorId TEXT NOT NULL,
    url TEXT NOT NULL,
    sharedWith TEXT,
    createdTime LONG NOT NULL,
    modifiedTime LONG NOT NULL,
    FOREIGN KEY (actorId) REFERENCES fb_page (id)
);

INSERT INTO fb_account VALUES (
    'gaurav.gen3@yahoo.com',
    'gaurav@yahoo123',
    'Gaurav Guleria'
);

INSERT INTO fb_page VALUES (
    '61573758161820',
    'Kame House Hub',
    'goku:roshi:vegeta',
    0,
    'gaurav.gen3@yahoo.com',
    '9350951804995107'
);

INSERT INTO fb_group VALUES (
    '9350951804995107',
    'DBZ Family',
    'gaurav.gen3@yahoo.com',
    0
);