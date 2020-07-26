-- AREA

CREATE TABLE musicbrainz.area_type (
    id                  SERIAL, -- PK
    name                VARCHAR(255) NOT NULL,
    parent              INTEGER, -- references area_type.id
    child_order         INTEGER NOT NULL DEFAULT 0,
    description         TEXT,
    gid                 uuid NOT NULL
);

INSERT INTO musicbrainz.area_type (id, name, parent, child_order, description, gid) VALUES (1, 'Country', NULL, 1, 'Country is used for areas included (or previously included) in ISO 3166-1, e.g. United States.', '06dd0ae4-8c74-30bb-b43d-95dcedf961de');

CREATE TABLE musicbrainz.area (
    id                  SERIAL, -- PK
    gid                 uuid NOT NULL,
    name                VARCHAR NOT NULL,
    type                INTEGER, -- references area_type.id
    edits_pending       INTEGER NOT NULL DEFAULT 0 CHECK (edits_pending >=0),
    last_updated        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    begin_date_year     SMALLINT,
    begin_date_month    SMALLINT,
    begin_date_day      SMALLINT,
    end_date_year       SMALLINT,
    end_date_month      SMALLINT,
    end_date_day        SMALLINT,
    ended               BOOLEAN NOT NULL DEFAULT FALSE
      CHECK (
        (
          -- If any end date fields are not null, then ended must be true
          (end_date_year IS NOT NULL OR
           end_date_month IS NOT NULL OR
           end_date_day IS NOT NULL) AND
          ended = TRUE
        ) OR (
          -- Otherwise, all end date fields must be null
          (end_date_year IS NULL AND
           end_date_month IS NULL AND
           end_date_day IS NULL)
        )
      ),
    comment             VARCHAR(255) NOT NULL DEFAULT ''
);

INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (10, '0df04709-c7d8-3b55-a6ea-f3e5069a947b', 'Argentina', 1, 0, '2013-05-27 14:05:48.558955+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (13, '106e0bec-b638-3b37-b731-f53d507dc00e', 'Australia', 1, 0, '2013-05-27 12:20:27.507257+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (38, '71bbafaa-e825-3e15-8ca9-017dcad1748b', 'Canada', 1, 0, '2013-05-27 13:15:52.179105+00', NULL, NULL,NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (73, '08310658-51eb-3801-80de-5a0739207115', 'France', 1, 0, '2013-05-27 12:50:32.702645+00', NULL, NULL,NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (81, '85752fda-13c4-31a3-bee5-0e5cb1f51dad', 'Germany', 1, 0, '2013-05-27 12:44:37.529747+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (100, 'd3a68bd0-7419-3f99-a5bd-204d6e057089', 'Indonesia', 1, 0, '2013-05-27 13:29:59.731299+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (107, '2db42837-c832-3c27-b4a3-08198f75693c', 'Japan', 1, 0, '2013-05-27 12:29:56.162248+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (138, '3e08b2cd-69f3-317c-b1e4-e71be581839e', 'Mexico', 1, 0, '2013-05-27 13:41:13.615269+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (150, 'ef1b7cc0-cd26-36f4-8ea0-04d9623786c7', 'Netherlands', 1, 0, '2013-05-27 13:06:47.020436+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (153, '8524c7d9-f472-3890-a458-f28d5081d9c4', 'New Zealand', 1, 0, '2013-05-27 12:21:18.909603+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (170, 'dd7f80c8-f017-3d01-8608-2a8c9c32b954', 'Poland', 1, 0, '2013-05-27 13:31:42.264869+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (171, '781b0c54-3d54-362d-a941-8a617def4992', 'Portugal', 1, 0, '2014-09-04 17:02:19.820793+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (176, '1f1fc3a4-9500-39b8-9f10-f0a465557eef', 'Russia', 1, 0, '2015-01-01 23:56:40.841959+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (194, '471c46a7-afc5-31c4-923c-d0444f5053a4', 'Spain', 1, 0, '2013-05-27 13:08:54.580681+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (221, '8a754a16-0027-3a29-b6d7-2b40ea0481ed', 'United Kingdom', 1, 0, '2013-05-16 11:06:19.67235+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (222, '489ce91b-6658-3307-9877-795b68554c98', 'United States', 1, 0, '2013-06-15 18:06:39.59323+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (240, '525d4e18-3d00-31b9-a58b-a146a916de8f', '[Worldwide]', NULL, 0, '2013-08-28 11:55:07.839087+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');
INSERT INTO musicbrainz.area (id, gid, name, type, edits_pending, last_updated, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, ended, comment) VALUES (241, '89a675c2-3e37-3518-b83c-418bad59a85a', 'Europe', NULL, 0, '2013-08-28 11:55:13.834089+00', NULL, NULL, NULL, NULL, NULL, NULL, false, '');

CREATE TABLE musicbrainz.country_area (
    area                INTEGER -- PK, references area.id
);

INSERT INTO musicbrainz.country_area (area) VALUES (81);
INSERT INTO musicbrainz.country_area (area) VALUES (241);
INSERT INTO musicbrainz.country_area (area) VALUES (221);
INSERT INTO musicbrainz.country_area (area) VALUES (138);
INSERT INTO musicbrainz.country_area (area) VALUES (240);
INSERT INTO musicbrainz.country_area (area) VALUES (153);
INSERT INTO musicbrainz.country_area (area) VALUES (10);
INSERT INTO musicbrainz.country_area (area) VALUES (13);
INSERT INTO musicbrainz.country_area (area) VALUES (222);
INSERT INTO musicbrainz.country_area (area) VALUES (100);
INSERT INTO musicbrainz.country_area (area) VALUES (38);
INSERT INTO musicbrainz.country_area (area) VALUES (150);
INSERT INTO musicbrainz.country_area (area) VALUES (171);
INSERT INTO musicbrainz.country_area (area) VALUES (107);
INSERT INTO musicbrainz.country_area (area) VALUES (170);
INSERT INTO musicbrainz.country_area (area) VALUES (194);
INSERT INTO musicbrainz.country_area (area) VALUES (176);
INSERT INTO musicbrainz.country_area (area) VALUES (73);

-- LANGUAGE

CREATE TABLE musicbrainz.language (
    id                  SERIAL,
    iso_code_2t         CHAR(3), -- ISO 639-2 (T)
    iso_code_2b         CHAR(3), -- ISO 639-2 (B)
    iso_code_1          CHAR(2), -- ISO 639
    name                VARCHAR(100) NOT NULL,
    frequency           INTEGER NOT NULL DEFAULT 0,
    iso_code_3          CHAR(3)  -- ISO 639-3
);

ALTER TABLE musicbrainz.language
      ADD CONSTRAINT iso_code_check
      CHECK (iso_code_2t IS NOT NULL OR iso_code_3  IS NOT NULL);

-- SCRIPT

CREATE TABLE musicbrainz.script (
    id                  SERIAL,
    iso_code            CHAR(4) NOT NULL, -- ISO 15924
    iso_number          CHAR(3) NOT NULL, -- ISO 15924
    name                VARCHAR(100) NOT NULL,
    frequency           INTEGER NOT NULL DEFAULT 0
);

-- GENDER

CREATE TABLE musicbrainz.gender (
    id                  SERIAL,
    name                VARCHAR(255) NOT NULL,
    parent              INTEGER, -- references gender.id
    child_order         INTEGER NOT NULL DEFAULT 0,
    description         TEXT,
    gid                 uuid NOT NULL
);

-- ARTIST

CREATE TABLE musicbrainz.artist_type (
    id                  SERIAL,
    name                VARCHAR(255) NOT NULL,
    parent              INTEGER, -- references artist_type.id
    child_order         INTEGER NOT NULL DEFAULT 0,
    description         TEXT,
    gid                 uuid NOT NULL
);

INSERT INTO musicbrainz.artist_type (id, name, parent, child_order, description, gid) VALUES (2, 'Group', NULL, 2, NULL, 'e431f5f6-b5d2-343d-8b36-72607fffb74b');

CREATE TABLE musicbrainz.artist (
    id                  SERIAL,
    gid                 UUID NOT NULL,
    name                VARCHAR NOT NULL,
    sort_name           VARCHAR NOT NULL,
    begin_date_year     SMALLINT,
    begin_date_month    SMALLINT,
    begin_date_day      SMALLINT,
    end_date_year       SMALLINT,
    end_date_month      SMALLINT,
    end_date_day        SMALLINT,
    type                INTEGER, -- references artist_type.id
    area                INTEGER, -- references area.id
    gender              INTEGER, -- references gender.id
    comment             VARCHAR(255) NOT NULL DEFAULT '',
    edits_pending       INTEGER NOT NULL DEFAULT 0 CHECK (edits_pending >= 0),
    last_updated        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ended               BOOLEAN NOT NULL DEFAULT FALSE
      CONSTRAINT artist_ended_check CHECK (
        (
          -- If any end date fields are not null, then ended must be true
          (end_date_year IS NOT NULL OR
           end_date_month IS NOT NULL OR
           end_date_day IS NOT NULL) AND
          ended = TRUE
        ) OR (
          -- Otherwise, all end date fields must be null
          (end_date_year IS NULL AND
           end_date_month IS NULL AND
           end_date_day IS NULL)
        )
      ),
    begin_area          INTEGER, -- references area.id
    end_area            INTEGER -- references area.id
);

INSERT INTO musicbrainz.artist (id, gid, name, sort_name, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, type, area, gender, comment, edits_pending, last_updated, ended, begin_area, end_area) VALUES (3544, 'd700b3f5-45af-4d02-95ed-57d301bda93e', 'Mogwai', 'Mogwai', 1995, NULL, NULL, NULL, NULL, NULL, 2, 221, NULL, 'Scottish post-rock band', 0, '2013-07-25 09:00:19.675349+00', false, 3855, NULL);
INSERT INTO musicbrainz.artist (id, gid, name, sort_name, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, type, area, gender, comment, edits_pending, last_updated, ended, begin_area, end_area) VALUES (16043, 'c14b4180-dc87-481e-b17a-64e4150f90f6', 'Opeth', 'Opeth', 1990, NULL, NULL, NULL, NULL, NULL, 2, 202, NULL, '', 0, '2013-06-15 11:00:18.966549+00', false, 5114, NULL);
INSERT INTO musicbrainz.artist (id, gid, name, sort_name, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, type, area, gender, comment, edits_pending, last_updated, ended, begin_area, end_area) VALUES (343789, '092ae9e2-60bf-4b66-aa33-9e31754d1924', 'Dead Combo', 'Dead Combo', 2003, NULL, NULL, NULL, NULL, NULL, 2, 171, NULL, 'Portuguese group', 0, '2015-07-18 02:00:45.563088+00', false, 5062, NULL);
INSERT INTO musicbrainz.artist (id, gid, name, sort_name, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, type, area, gender, comment, edits_pending, last_updated, ended, begin_area, end_area) VALUES (14521, '7bbfd77c-1102-4831-9ba8-246fb67460b3', 'Ayreon', 'Ayreon', 1995, NULL, NULL, NULL, NULL, NULL, 2, 150, NULL, '', 0, '2018-03-31 19:52:14.32+00', false, 150, NULL);
INSERT INTO musicbrainz.artist (id, gid, name, sort_name, begin_date_year, begin_date_month, begin_date_day, end_date_year, end_date_month, end_date_day, type, area, gender, comment, edits_pending, last_updated, ended, begin_area, end_area) VALUES (10042, 'ca891d65-d9b0-4258-89f7-e6ba29d83767', 'Iron Maiden', 'Iron Maiden', 1975, 12, 25, NULL, NULL, NULL, 2, 221, NULL, 'English heavy metal band', 0, '2016-01-02 02:00:41.207128+00', false, 55361, NULL);

-- ARTIST CREDIT

CREATE TABLE musicbrainz.artist_credit (
    id                  SERIAL,
    name                VARCHAR NOT NULL,
    artist_count        SMALLINT NOT NULL,
    ref_count           INTEGER DEFAULT 0,
    created             TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    edits_pending       INTEGER NOT NULL DEFAULT 0 CHECK (edits_pending >= 0)
);

INSERT INTO musicbrainz.artist_credit (id, name, artist_count, ref_count, created, edits_pending) VALUES (14521, 'Ayreon', 1, 2754, '2011-05-16 16:32:11.963929+00', 0);
INSERT INTO musicbrainz.artist_credit (id, name, artist_count, ref_count, created, edits_pending) VALUES (16043, 'Opeth', 1, 3502, '2011-05-16 16:32:11.963929+00', 0);
INSERT INTO musicbrainz.artist_credit (id, name, artist_count, ref_count, created, edits_pending) VALUES (343789, 'Dead Combo', 1, 352, '2011-05-16 16:32:11.963929+00', 0);
INSERT INTO musicbrainz.artist_credit (id, name, artist_count, ref_count, created, edits_pending) VALUES (3544, 'Mogwai', 1, 3322, '2011-05-16 16:32:11.963929+00', 0);
INSERT INTO musicbrainz.artist_credit (id, name, artist_count, ref_count, created, edits_pending) VALUES (10042, 'Iron Maiden', 1, 10639, '2011-05-16 16:32:11.963929+00', 0);

-- RELEASE_GROUP

CREATE TABLE musicbrainz.release_group_primary_type (
    id                  SERIAL,
    name                VARCHAR(255) NOT NULL,
    parent              INTEGER, -- references release_group_primary_type.id
    child_order         INTEGER NOT NULL DEFAULT 0,
    description         TEXT,
    gid                 uuid NOT NULL
);

INSERT INTO musicbrainz.release_group_primary_type (id, name, parent, child_order, description, gid) VALUES (1, 'Album', NULL, 1, NULL, 'f529b476-6e62-324f-b0aa-1f3e33d313fc');

CREATE TABLE musicbrainz.release_group_secondary_type (
    id                  SERIAL NOT NULL, -- PK
    name                TEXT NOT NULL,
    parent              INTEGER, -- references release_group_secondary_type.id
    child_order         INTEGER NOT NULL DEFAULT 0,
    description         TEXT,
    gid                 uuid NOT NULL
);

CREATE TABLE musicbrainz.release_group_secondary_type_join (
    release_group INTEGER NOT NULL, -- PK, references release_group.id,
    secondary_type INTEGER NOT NULL, -- PK, references release_group_secondary_type.id
    created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE musicbrainz.release_group ( -- replicate (verbose)
    id                  SERIAL,
    gid                 UUID NOT NULL,
    name                VARCHAR NOT NULL,
    artist_credit       INTEGER NOT NULL, -- references artist_credit.id
    type                INTEGER, -- references release_group_primary_type.id
    comment             VARCHAR(255) NOT NULL DEFAULT '',
    edits_pending       INTEGER NOT NULL DEFAULT 0 CHECK (edits_pending >= 0),
    last_updated        TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

INSERT INTO musicbrainz.release_group (id, gid, name, artist_credit, type, comment, edits_pending, last_updated) VALUES (57829, '68b9f75b-34b5-3228-9972-82efea767eca', 'Come On Die Young', 3544, 1, '', 0, '2014-12-03 14:03:11.550329+00');
INSERT INTO musicbrainz.release_group (id, gid, name, artist_credit, type, comment, edits_pending, last_updated) VALUES (384931, '00f78b7d-bd0a-356a-aec4-925e529023f8', 'Ghost Reveries', 16043, 1, '', 0, '2010-10-15 13:02:32.333198+00');
INSERT INTO musicbrainz.release_group (id, gid, name, artist_credit, type, comment, edits_pending, last_updated) VALUES (508330, '4adf1192-df7a-3967-a8e6-d39963c62994', 'Vol. II - Quando a alma não é pequena', 343789, 1, '', 0, '2009-05-24 20:47:00.490177+00');
INSERT INTO musicbrainz.release_group (id, gid, name, artist_credit, type, comment, edits_pending, last_updated) VALUES (1309730, '6281bcfe-058e-4cd3-85bc-66f47c28960b', 'The Theory of Everything', 14521, 1, '', 0, '2013-10-16 18:35:23.061087+00');
INSERT INTO musicbrainz.release_group (id, gid, name, artist_credit, type, comment, edits_pending, last_updated) VALUES (34849, '4ebfe175-e7ed-34cd-8e91-67c7e4a53579', 'The Number of the Beast', 10042, 1, '', 0, '2014-06-07 10:02:32.172033+00');

-- RELEASE

CREATE TABLE musicbrainz.release_status (
    id                  SERIAL,
    name                VARCHAR(255) NOT NULL,
    parent              INTEGER, -- references release_status.id
    child_order         INTEGER NOT NULL DEFAULT 0,
    description         TEXT,
    gid                 uuid NOT NULL
);

INSERT INTO musicbrainz.release_status (id, name, parent, child_order, description, gid) VALUES (1, 'Official', NULL, 1, 'Any release officially sanctioned by the artist and/or their record company. Most releases will fit into this category.', '4e304316-386d-3409-af2e-78857eec5cfe');

CREATE TABLE musicbrainz.release_packaging (
    id                  SERIAL,
    name                VARCHAR(255) NOT NULL,
    parent              INTEGER, -- references release_packaging.id
    child_order         INTEGER NOT NULL DEFAULT 0,
    description         TEXT,
    gid                 uuid NOT NULL
);

INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (16, 'Super Jewel Box', NULL, 0, NULL, 'dfb7da53-866f-4dfd-a016-80bafaeff3db');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (3, 'Digipak', NULL, 0, NULL, '8f931351-d2e2-310f-afc6-37b89ddba246');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (6, 'Keep Case', NULL, 0, NULL, 'bb14bb17-e8ad-375f-a3c6-b1f82fd2bcc4');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (4, 'Cardboard/Paper Sleeve', NULL, 0, NULL, 'f7101ce3-0384-39ce-9fde-fbbd0044d35f');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (8, 'Cassette Case', NULL, 0, NULL, 'c70b737a-0114-39a9-88f7-82843e54f906');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (9, 'Book', NULL, 0, NULL, 'd60b6157-79fe-3913-ab8b-23b32de8690d');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (10, 'Fatbox', NULL, 0, NULL, '57429523-ffe6-3336-9381-32565c142c18');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (11, 'Snap Case', NULL, 0, NULL, '935f2847-8083-3422-8f0d-d7516fcda682');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (12, 'Gatefold Cover', NULL, 0, NULL, 'e724a489-a7e8-30a1-a17c-30dfd6831202');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (13, 'Discbox Slider', NULL, 0, NULL, '21179778-2f98-3d11-816e-42b469a0c924');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (5, 'Other', NULL, 1, NULL, '815b7785-8284-3926-8f04-e48bc6c4d102');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (7, 'None', NULL, 2, NULL, '119eba76-b343-3e02-a292-f0f00644bb9b');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (1, 'Jewel Case', NULL, 0, 'The traditional CD case, made of hard, brittle plastic.', 'ec27701a-4a22-37f4-bfac-6616e0f9750a');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (2, 'Slim Jewel Case', NULL, 0, 'A thinner jewel case, commonly used for CD singles.', '36327bc2-f691-3d66-80e5-bd03cec6060a');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (17, 'Digibook', NULL, 0, 'A perfect bound book with a sleeve at the end to hold a CD', '9f2e13bc-f84f-428a-8342-fd86ece7fc4d');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (18, 'Plastic Sleeve', NULL, 0, NULL, 'bf996342-d111-4d37-b9d6-d759f0787533');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (19, 'Box', NULL, 0, NULL, 'c1668fc7-8944-4a00-bc3e-46e8d861d211');
INSERT INTO musicbrainz.release_packaging (id, name, parent, child_order, description, gid) VALUES (20, 'Slidepack', NULL, 0, 'Plastic CD tray inside a cardboard slipcover', '2aee93e9-8acb-476c-807e-6a4a3974e1cb');

CREATE TABLE musicbrainz.release (
    id                  SERIAL,
    gid                 UUID NOT NULL,
    name                VARCHAR NOT NULL,
    artist_credit       INTEGER NOT NULL, -- references artist_credit.id
    release_group       INTEGER NOT NULL, -- references release_group.id
    status              INTEGER, -- references release_status.id
    packaging           INTEGER, -- references release_packaging.id
    language            INTEGER, -- references language.id
    script              INTEGER, -- references script.id
    barcode             VARCHAR(255),
    comment             VARCHAR(255) NOT NULL DEFAULT '',
    edits_pending       INTEGER NOT NULL DEFAULT 0 CHECK (edits_pending >= 0),
    quality             SMALLINT NOT NULL DEFAULT -1,
    last_updated        TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (61057, 'e3913b41-63eb-49f9-a4fd-f0952e2fdc9b', 'Come On Die Young', 3544, 57829, 1, 1, 120, 28, '744861036524', '', 0, -1, '2015-02-07 19:00:23.432828+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (326205, '4e27079e-97b4-31b7-a433-1b259d4a2105', 'Come On Die Young', 3544, 57829, 1, NULL, 120, 28, '9399602466729', '', 0, -1, '2013-01-07 10:56:18.832122+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (357613, '608e3b28-ec93-4bf6-b424-e6f075d0925b', 'Come On Die Young', 3544, 57829, 1, NULL, 120, 28, '5020667343314', '', 0, -1, '2015-04-07 15:00:48.955807+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (489817, 'f02525e8-4261-34eb-9e02-0273404b2ce2', 'Come On Die Young', 3544, 57829, 1, NULL, 120, 28, '724384729523', '', 0, -1, '2014-09-23 14:00:43.949727+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1453411, '52632f05-8649-4c37-8c65-7645c0c422d7', 'Come On Die Young', 3544, 57829, 1, 1, 120, 28, '5024545691726', 'deluxe edition', 0, -1, '2015-08-12 20:01:14.177557+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1323946, '029b6d5e-6471-4ec0-b532-badce492a0a6', 'Come On Die Young', 3544, 57829, 1, NULL, 120, 28, '4988004108419', '', 0, -1, '2017-11-22 22:00:44.55667+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1523367, '77042ce7-138e-485f-8a8f-de77f979a83d', 'Come On Die Young', 3544, 57829, 1, 7, 120, 28, '', 'deluxe edition', 0, -1, '2016-02-19 11:08:48.626835+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (122106, '3e24ce0c-8c65-3d11-a595-bd404d8695cc', 'Come On Die Young', 3544, 57829, 1, NULL, 120, 28, '5020667343321', '', 0, -1, '2016-06-27 14:00:44.654678+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (2375645, '43eb4548-0e56-403f-89bb-69313b8d3621', 'Come On Die Young', 3544, 57829, 1, 1, 120, 28, '5020667343321', '', 0, -1, '2019-03-24 16:00:38.488658+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1453408, '0f5954d8-8f01-4937-bb59-c4903223d590', 'Come On Die Young', 3544, 57829, 1, NULL, 120, 28, NULL, '', 0, -1, '2020-01-23 14:00:19.246132+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (212980, '88519812-f5fa-3ce9-85d5-9f04b1a8d7a9', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '4527583005957', '', 0, -1, '2010-02-11 00:08:11.779827+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (212982, '7f2c9f63-53f1-3382-8e26-9ff6f32cd619', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, NULL, '', 0, -1, '2019-10-24 12:00:21.404881+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (539385, 'f2ec220d-132c-3808-adbe-67c0a16b7231', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '4527583006954', '', 0, -1, '2010-02-11 00:08:11.779827+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (539388, 'bb8cbf7a-8a21-31b8-8f8f-0b5a2f99432e', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '4527583006473', '', 0, -1, '2014-05-14 15:00:18.953072+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (540398, 'cbe82a52-846d-34ea-be67-b013ac5516d1', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '016861812355', '', 0, -1, '2009-12-06 01:26:18.951492+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (580572, 'fe99de2d-c939-3b88-a776-9be12d84907f', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '016861812355', '', 0, -1, '2009-12-06 01:26:18.951492+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (580574, '049d8226-b20b-3c5a-b4bf-6dd7e951e07c', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '452758300647', '', 0, -1, '2009-12-06 01:26:18.951492+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (581026, '8dc3be1f-385a-3d02-b01c-b572a09cea36', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '4605026007659', '', 0, -1, '2010-02-11 00:08:11.779827+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (581028, 'd01d6f97-3c92-330b-946e-dcb980d43f67', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '016861812324', '', 0, -1, '2010-02-11 00:08:11.779827+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (581033, 'c8da725e-d9fa-3c20-821c-071b4e124f0d', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '0016861812324', '', 0, -1, '2010-02-11 00:08:11.779827+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (581027, '5877d386-bef1-3e77-8f21-b07d9b72e289', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '0016861812324', '', 0, -1, '2018-11-08 04:00:39.724227+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (581038, '8540649a-edf1-3aff-bd66-1f4c72fb573f', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '899312293373', '', 0, -1, '2010-02-11 00:08:11.779827+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (649405, 'f2c96f9f-01a2-3761-a8a8-5b831343472c', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '016861812317', '', 0, -1, '2014-11-03 12:09:55.748276+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1075692, '7ea459bb-d340-4ac5-8007-c06bed2e474e', 'Ghost Reveries', 16043, 384931, 1, 1, 120, 28, '016861810221', '', 0, -1, '2017-04-08 20:00:46.284434+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (54578, '39858f32-d9f0-41e1-ad92-aabc0740bcb1', 'Ghost Reveries', 16043, 384931, 1, 1, 120, 28, '016861812324', '', 0, -1, '2014-02-10 22:00:13.005105+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (2095583, 'fb9907a6-3589-49f9-b7a3-5dd33d9f4afd', 'Ghost Reveries', 16043, 384931, 1, 7, 120, 28, '', '', 0, -1, '2018-07-30 16:02:04.166576+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (539389, 'f2641df9-fb9a-3ea2-b82c-6b3fdcb7f561', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '016861807825', '', 0, -1, '2016-10-30 19:00:42.038133+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (212981, '29ff94ee-d819-4b43-b465-6179919f6033', 'Ghost Reveries', 16043, 384931, 1, 3, 120, 28, '016861812355', '', 0, -1, '2017-01-13 01:00:24.052887+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (581030, '2f087439-5d1b-38c7-99fd-0de138a11fc2', 'Ghost Reveries', 16043, 384931, 1, NULL, 120, 28, '016861812324', '', 0, -1, '2018-09-10 05:00:16.552731+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (139488, '0d082370-0cfe-40ac-90e3-e55f49d505fb', 'Vol. II - Quando a alma não é pequena', 343789, 508330, 1, NULL, 340, 28, '602498773710', '', 0, -1, '2018-10-26 23:29:28.657654+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1347659, '9508133a-ce2e-49cc-ab0c-6778095dc489', 'The Theory of Everything', 14521, 1309730, 1, 9, 120, 28, '5052205066690', 'limited 4 CD + DVD + artbook edition', 0, -1, '2013-11-07 12:00:26.119471+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1345075, '57204449-552b-4b9d-9af8-968901f275c5', 'The Theory of Everything', 14521, 1309730, 1, 3, 120, 28, '5052205066607', 'Mediabook', 0, -1, '2019-09-05 19:19:52.404536+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1469842, '011624a1-ebc8-4bfb-94a2-8a7009779359', 'The Theory of Everything', 14521, 1309730, NULL, 7, 120, 28, '', '', 0, -1, '2018-04-09 13:00:23.081597+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1477386, 'b76810b6-9474-4cd0-a52b-016ace0c6b14', 'The Theory of Everything', 14521, 1309730, 1, 12, 120, 28, '5052205066614', '', 0, -1, '2014-09-20 21:01:10.163853+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1756696, 'e734f765-76a4-4a1f-8930-04f29f70862f', 'The Theory of Everything', 14521, 1309730, 1, 1, 120, 28, '', '', 0, -1, '2016-04-09 13:00:19.689042+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1730616, '50c934ed-182c-41b3-a8f8-9871da668089', 'The Theory of Everything', 14521, 1309730, 1, 1, 120, 28, '5052205066621', '', 0, -1, '2016-02-22 13:00:20.922108+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1342209, '8b828d3b-810d-4731-a51f-2b3d5d55193f', 'The Theory of Everything', 14521, 1309730, 1, 1, 120, 28, '5052205066621', '', 0, -1, '2017-08-09 15:00:20.457993+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1942043, '3196b534-3184-4f06-94c4-22c38cfb3c9f', 'The Theory of Everything', 14521, 1309730, 1, 3, 120, 28, '885417066623', '', 0, -1, '2017-04-08 10:00:34.600524+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (177204, 'aef95dd4-aa19-380a-9265-2437dc17c97c', 'The Number of the Beast', 10042, 34849, 1, NULL, 120, 28, NULL, '', 0, -1, '2015-08-12 16:01:24.07612+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (177202, '898d55cf-cd58-3026-a2fd-b8bcfea277b5', 'The Number of the Beast', 10042, 34849, 1, NULL, 120, 28, NULL, '', 0, -1, '2015-08-12 16:01:30.956833+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (177205, '3f2a8693-06d4-316d-9fc9-88e75eeaed71', 'The Number of the Beast', 10042, 34849, 1, NULL, 120, 28, NULL, '', 0, -1, '2015-08-12 16:01:33.088476+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (41776, '39eddca0-ea3e-44f7-9089-2d21eaee9caa', 'The Number of the Beast', 10042, 34849, 1, NULL, 120, 28, NULL, '', 0, -1, '2015-08-12 16:01:28.698304+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (729281, '310a7a07-47ba-3a19-8ef1-1039fb5dd6bf', 'The Number of the Beast', 10042, 34849, 1, 4, 120, 28, NULL, '', 0, -1, '2015-08-12 16:01:30.830096+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (729175, '19f90656-d7f9-36d4-a15a-928a2daec54b', 'The Number of the Beast', 10042, 34849, 1, NULL, 120, 28, NULL, '', 0, -1, '2011-01-30 14:58:25.865155+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (177207, 'e6e9e70a-91e7-3644-84c6-05d0f8847b8f', 'The Number of the Beast', 10042, 34849, 1, NULL, 120, 28, '600234010426', '', 0, -1, '2017-12-22 17:00:15.897131+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (166393, '0cea1780-d3ca-30a7-909d-6abcd7ee6a9b', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '077774636421', '', 0, -1, '2019-03-03 22:00:53.384568+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1121010, 'a222f11b-be36-47d4-a2de-7874e4b6a2e5', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '077774636421', '', 0, -1, '2017-05-05 23:00:32.652749+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (266741, '20d20196-ee7b-3069-aaca-943b8ee5a28a', 'The Number of the Beast', 10042, 34849, 1, NULL, 120, 28, NULL, '', 0, -1, '2015-08-12 16:01:23.731339+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (798016, '3f217777-6e64-3a3b-9c2c-1753df40ee49', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '', '', 0, -1, '2018-02-06 12:00:22.534757+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (194576, '4c07f2b6-5a38-48d6-83a4-c04265132289', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '696998621022', '', 0, -1, '2017-08-27 00:48:23.503704+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (177203, 'b8ed3cfc-0c67-39f0-ae28-bbd1bbd3cc86', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '077774636421', '', 0, -1, '2015-08-12 16:01:33.393512+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (194580, '3389b626-3fba-3f3c-87ca-b44ee4ac9e91', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '724349691803', '', 0, -1, '2015-05-26 22:01:09.097115+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1144461, '6f04f91d-77c9-48b8-9b2a-420b473d5ae8', 'The Number of the Beast', 10042, 34849, 1, 4, 120, 28, '5099910760819', '', 0, -1, '2014-02-11 19:53:24.246003+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (266742, '5aa782e6-d87a-3ec1-af46-ff68c76cf8fc', 'The Number of the Beast', 10042, 34849, 1, 4, 120, 28, '', '', 0, -1, '2015-08-12 16:01:26.380392+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1462884, 'e7c38fe7-8399-4103-a03c-c26a50f81851', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '077774636421', '', 0, -1, '2014-08-15 07:00:39.8201+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1630194, 'eb8b0a99-0c1e-4cc8-9dbc-afdbb6c221ac', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '07777463642', '', 0, -1, '2015-07-15 00:01:04.574255+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (194583, '59f164b0-ac13-3f9b-a632-51e45ea85d1d', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '724349691803', '', 0, -1, '2017-12-22 16:00:23.36568+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (194584, 'b54fd218-4ad8-32b9-bb4b-6138c9e2cefa', 'The Number of the Beast', 10042, 34849, 1, NULL, 120, 28, '602923012926', '', 0, -1, '2014-08-27 22:00:25.571575+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1661427, '92f80666-3610-413a-9c95-119bf10f8dd3', 'The Number of the Beast', 10042, 34849, 1, 4, 120, 28, '', '', 0, -1, '2015-09-28 15:00:12.686234+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1342081, '9671a025-bb1e-42d5-91b1-74be89062433', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '077774636421', '', 0, -1, '2015-08-11 13:00:35.898562+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1770000, 'd387a3d1-2360-408c-aa36-8a0e68b6040b', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '724349691803', '', 0, -1, '2016-05-08 16:00:32.392515+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1481974, 'd9216f7b-86c0-4b3c-b5a4-76177c671776', 'The Number of the Beast', 10042, 34849, 1, 12, 120, 28, NULL, '', 0, -1, '2014-09-26 21:00:30.581348+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (177206, '14d0dbd8-75e7-3dd0-928c-885bba806efb', 'The Number of the Beast', 10042, 34849, 1, NULL, 120, 28, '724383587025', '', 0, -1, '2017-12-22 16:00:23.949332+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1832868, 'e38f0fda-a3f2-4726-82e1-cd68a0428ed6', 'The Number of the Beast', 10042, 34849, NULL, NULL, NULL, NULL, '825646252404', '', 0, -1, '2016-09-26 21:00:47.891633+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (475144, 'e6ba3c16-47e5-33f8-9234-7db027c18632', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '602923012926', '', 0, -1, '2016-11-10 13:00:19.203808+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (1916663, 'e0cff9f3-b9fe-4254-add3-30783456b200', 'The Number of the Beast', 10042, 34849, 1, NULL, NULL, NULL, '', '', 0, -1, '2017-09-21 08:00:13.773768+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (2381660, '8c339ace-f9fc-41d1-9af3-0bd5dee8455d', 'The Number of the Beast', 10042, 34849, 1, NULL, 120, 28, '077774636421', '', 0, -1, '2019-03-25 08:22:56.051871+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (493662, '324e74bf-f2c5-36bb-ad66-d20348e2a6db', 'The Number of the Beast', 10042, 34849, 1, 4, 120, 28, '', '', 0, -1, '2018-10-15 15:00:53.964243+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (2290872, 'cd36157d-cd89-44be-a9cb-85e1fbe3e86f', 'The Number of the Beast', 10042, 34849, 1, 3, 120, 28, '0190295567743', '', 0, -1, '2018-11-18 18:47:16.179664+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (2401659, '1ace82cf-9c66-47c2-9361-8472b48c35a9', 'The Number of the Beast', 10042, 34849, 1, 7, 120, 28, '190295524241', '2015 remaster', 0, -1, '2019-12-02 03:20:09.232761+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (2331862, '9fee8211-063d-4af0-a882-56e472a88355', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '077774636421', '', 0, -1, '2019-01-20 14:21:12.02603+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (2334124, 'cf4a2c5c-5814-4b14-a66a-20176f185679', 'The Number of the Beast', 10042, 34849, 1, 4, 120, 28, '', '', 0, -1, '2019-01-30 20:00:53.156696+00');
INSERT INTO musicbrainz.release (id, gid, name, artist_credit, release_group, status, packaging, language, script, barcode, comment, edits_pending, quality, last_updated) VALUES (2584050, 'b94231f4-8cbc-4c5a-989b-96cd26beabe4', 'The Number of the Beast', 10042, 34849, 1, 1, 120, 28, '0208314105271', '', 0, -1, '2020-01-11 21:00:26.260478+00');

CREATE TABLE musicbrainz.release_country (
  release INTEGER NOT NULL,  -- PK, references release.id
  country INTEGER NOT NULL,  -- PK, references country_area.area
  date_year SMALLINT,
  date_month SMALLINT,
  date_day SMALLINT
);

INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (61057, 222, 1999, 4, 6);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (326205, 13, 1999, 4, 6);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (357613, 221, 1999, 3, 29);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (489817, 73, 1999, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1453411, 221, 2014, 7, 21);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1323946, 107, 2008, 9, 24);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1523367, 240, 2014, 7, 18);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (122106, 221, 1999, 3, 29);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (2375645, 81, 1999, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1453408, 221, 2014, 7, 21);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (212980, 107, 2005, 9, 7);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (212982, 221, 2006, 10, 30);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (539385, 107, 2007, 5, 2);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (539388, 107, 2006, 11, 15);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (540398, 170, 2006, 10, 30);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (580572, 138, 2006, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (580574, 153, 2006, 10, 31);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (581026, 176, 2005, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (581028, 150, 2005, 8, 27);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (581033, 194, 2006, 10, 30);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (581027, 13, 2005, 8, 29);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (581038, 100, 2005, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (649405, 150, 2005, 8, 29);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1075692, 222, 2005, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (54578, 221, 2005, 8, 29);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (2095583, 81, 2005, 8, 26);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (539389, 222, 2006, 10, 31);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (212981, 81, 2006, 10, 27);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (581030, 38, 2005, 8, 27);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (581030, 222, 2005, 8, 30);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (139488, 171, 2006, 3, 20);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1347659, 241, 2013, 10, 25);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1345075, 241, 2013, 10, 25);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1469842, 81, 2013, 10, 28);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1477386, 81, 2013, 9, 25);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1756696, 176, 2013, 12, 4);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1730616, 10, 2013, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1342209, 81, 2013, 10, 25);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1342209, 222, 2013, 10, 28);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1342209, 241, 2013, 10, 25);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1481974, 222, 1982, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (729281, 73, 1982, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (798016, 107, 1986, 3, 1);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1770000, 10, 1998, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (194576, 222, 2002, 3, 26);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1916663, 240, 2015, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (41776, 81, 1982, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (266742, 221, 1982, 3, 29);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (177203, 222, 1987, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (177207, 222, 1995, 10, 9);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (2381660, 221, NULL, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (177206, 221, 1995, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (194584, 81, 1998, 9, 22);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (194583, 221, 1998, 9, 14);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (493662, 171, 1982, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1144461, 241, 1988, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (177202, 150, 1982, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1630194, 38, 1987, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (166393, 221, 1994, 7, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (2334124, 241, 1982, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1661427, 176, 1993, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (266741, 153, 1982, 3, 29);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (475144, 222, 1998, 9, 29);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1342081, 221, 1987, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (2290872, 241, 2018, 11, 16);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (2331862, 150, NULL, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1462884, 150, 1987, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (194580, 38, 1998, 9, 15);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (2584050, 222, NULL, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (2401659, 138, 2018, 11, 16);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (2401659, 221, 2018, 11, 16);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1832868, 241, 2014, 10, 13);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (177205, 81, 1985, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (1121010, 150, 1982, NULL, NULL);
INSERT INTO musicbrainz.release_country (release, country, date_year, date_month, date_day) VALUES (177204, 153, 1982, 3, 29);

CREATE TABLE musicbrainz.release_unknown_country (
  release INTEGER NOT NULL,  -- PK, references release.id
  date_year SMALLINT,
  date_month SMALLINT,
  date_day SMALLINT
);

INSERT INTO musicbrainz.release_unknown_country (release, date_year, date_month, date_day) VALUES (729175, 1982, NULL, NULL);