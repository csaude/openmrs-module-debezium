CREATE TABLE location (
    id int (11) NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    uuid varchar(38) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE person (
    id int (11) NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE encounter_type (
    id int (11) NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO location(name,description,uuid) VALUES('Demo', 'Unknown', 'ab3b12d1-5c4f-415f-871b-b98a22137604');
