--	Create database
--	Use unicode encoding so we can have nice names like Вељко or Ђорђе
CREATE DATABASE risk WITH ENCODING = 'UTF8';

\connect risk;

--	#####################################################
--	---------------------
--	|		UserType	|
--	---------------------
--	| PK	Name		|
--	#####################################################
CREATE TABLE UserTypes
(
	Name		varchar(32),
	
	PRIMARY KEY (Name)
);

--	#####################################################
--	---------------------
--	|		Users		|
--	---------------------
--	| PK	ID			|
--	| FK	Type		|
--	#####################################################
CREATE TABLE Users
(
	ID			SERIAL,
	Type		varchar(32)	REFERENCES UserTypes(Name) ON UPDATE CASCADE,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|	FacebookUser	|
--	---------------------
--	| PK	ID			|
--	|		FacebookID	|
--	#####################################################
CREATE TABLE FacebookUser
(
	ID			integer	REFERENCES Users(ID) ON UPDATE CASCADE,
	FacebookID	integer	UNIQUE,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|	OffensiveUser	|
--	---------------------
--	| PK	ID			|
--	|		FacebookID	|
--	#####################################################
CREATE TABLE OffensiveUser
(
	ID			integer		REFERENCES Users(ID) ON UPDATE CASCADE,
	UserName	varchar(32)	UNIQUE,
	Password	bytea,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|		Colors		|
--	---------------------
--	| PK	Name		|
--	#####################################################
CREATE TABLE Colors
(
	Name	varchar(32),
	
	PRIMARY KEY (Name)
);

--	#####################################################
--	---------------------
--	|		Players		|
--	---------------------
--	| PK	ID			|
--	| FK	UserName	|
--	| FK	Color		|
--	#####################################################
CREATE TABLE Players
(
	ID			SERIAL,
	UserName	integer		REFERENCES Users(ID) ON UPDATE CASCADE,
	Color		varchar(32)	REFERENCES Colors(Name) ON UPDATE CASCADE,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|		Fields		|
--	---------------------
--	| PK	ID			|
--	|		Name		|
--	#####################################################
CREATE TABLE Fields
(
	ID		SERIAL,
	Name	varchar(32),
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	-------------------------
--	|		Connections		|
--	-------------------------
--	| PK FK	Field1			|
--	| PK FK	Field2			|
--	#####################################################
CREATE TABLE Connections
(
	Field1	integer	REFERENCES Fields(ID) ON UPDATE CASCADE,
	Field2	integer	REFERENCES Fields(ID) ON UPDATE CASCADE,
	
	PRIMARY KEY (Field1, Field2),
	CHECK (Field1<>Field2)
);

--	#####################################################
--	---------------------
--	|		Boards		|
--	---------------------
--	| PK	ID			|
--	#####################################################
CREATE TABLE Boards
(
	ID	SERIAL,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|		HasFields	|
--	---------------------
--	| PK FK	Board		|
--	| PK FK	Field		|
--	#####################################################
CREATE TABLE HasFields
(
	Board	integer REFERENCES Boards(ID) ON UPDATE CASCADE,
	Field	integer REFERENCES Fields(ID) ON UPDATE CASCADE,
	
	PRIMARY KEY (Board, Field)
);

--	#####################################################
--	---------------------
--	|		Phases		|
--	---------------------
--	| PK	Name		|
--	#####################################################
CREATE TABLE Phases
(
	Name	varchar(32),
	
	PRIMARY KEY (Name)
);

--	#####################################################
--	-------------------------
--	|		Games			|
--	-------------------------
--	| PK	ID				|
--	| FK	Phase			|
--	| FK	Board			|
--	| 		CurrentRound	|
--	#####################################################
CREATE TABLE Games
(
	ID				SERIAL,
	Phase			varchar(32)	REFERENCES Phases(Name) ON UPDATE CASCADE,
	Board			integer		REFERENCES Boards(ID) 	ON UPDATE CASCADE,
	CurrentRound	smallint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|		Plays		|
--	---------------------
--	| PK FK	Game		|
--	| PK FK	Player		|
--	#####################################################
CREATE TABLE Plays
(
	Game	integer REFERENCES Games(ID) ON UPDATE CASCADE,
	Player	integer REFERENCES Players(ID) ON UPDATE CASCADE,
	
	PRIMARY KEY (Game, Player)
);

--	#####################################################
--	---------------------------------
--	|		TroopDeployments		|
--	---------------------------------
--	| PK FK	Game					|
--	| PK FK	Field					|
--	| FK	Player					|
--	| 		TroopNumber				|
--	#####################################################
CREATE TABLE TroopDeployments
(
	Game		integer	REFERENCES Games(ID)	ON UPDATE CASCADE,
	Field		integer	REFERENCES Fields(ID) 	ON UPDATE CASCADE,
	Player		integer	REFERENCES Players(ID) 	ON UPDATE CASCADE,
	TroopNumber	smallint,
	
	PRIMARY KEY (Game, Field)
);

--	#####################################################
--	-------------------------
--	|		Commands		|
--	-------------------------
--	| PK FK	Game			|
--	| PK	Round			|
--	| PK FK	Player			|
--	| FK	Source			|
--	| FK	Destination		|
--	| 		TroopNumber		|
--	#####################################################
CREATE TABLE Commands
(
	Game		integer	REFERENCES Games(ID)	ON UPDATE CASCADE,
	"Round"		smallint,
	Player		integer	REFERENCES Players(ID) 	ON UPDATE CASCADE,
	Source		integer	REFERENCES Fields(ID)	ON UPDATE CASCADE,
	Destination	integer	REFERENCES Fields(ID)	ON UPDATE CASCADE,
	TroopNumber	smallint,
	
	PRIMARY KEY (Game, "Round", Player),
	CHECK (Source<>Destination)
);