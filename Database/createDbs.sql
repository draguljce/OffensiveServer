--	Create database
--	Use unicode encoding so we can have nice names like Вељко or Ђорђе
CREATE DATABASE offensive WITH ENCODING = 'UTF8';

\connect offensive;

--	#####################################################
--	---------------------
--	|		UserType	|
--	---------------------
--	| PK	Name		|
--	#####################################################
CREATE TABLE UserTypes
(
	Name		varchar(32),
	Version		bigint,
	
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
	ID			BIGSERIAL,
	Type		varchar(32)	REFERENCES UserTypes(Name) ON UPDATE CASCADE,
	Version		bigint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|	FacebookUsers	|
--	---------------------
--	| PK	FacebookID	|
--	| 		userID		|
--	#####################################################
CREATE TABLE FacebookUsers
(
	FacebookID	bigint,
	UserID		bigint	REFERENCES Users(ID) ON UPDATE CASCADE,
	Version		bigint,
	
	PRIMARY KEY (FacebookID)
);

CREATE UNIQUE INDEX facebookUsersIndex ON FacebookUsers (UserID);

--	#####################################################
--	---------------------
--	|	OffensiveUsers	|
--	---------------------
--	| PK	ID			|
--	| 		UserName	|
--	|		PassHash	|
--	|		Version		|
--	#####################################################
CREATE TABLE OffensiveUsers
(
	ID			bigint		REFERENCES Users(ID) ON UPDATE CASCADE,
	UserName	varchar(32)	UNIQUE,
	Password	varchar(512),
	Version		bigint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|		Colors		|
--	---------------------
--	| PK	ID			|
--	|   	Name		|
--	#####################################################
CREATE TABLE Colors
(
	ID		SERIAL,
	Name	varchar(32),
	Version		bigint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|		Phases		|
--	---------------------
--	| PK	ID		|
--	| 		Name		|
--	#####################################################
CREATE TABLE Phases
(
	ID		int,
	Name	varchar(32),
	Version		bigint,
	
	PRIMARY KEY (ID)
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
	Version		bigint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|		Objectives	|
--	---------------------
--	| PK	Description	|
--	#####################################################
CREATE TABLE Objectives
(
	ID			integer,
	Description	varchar(512),
	Version		bigint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------------------
--	|		CurrentGames			|
--	---------------------------------
--	| PK	ID						|
--	|   	GameName				|
--	|   	NumberOfJoinedPlayers	|
--	|   	NumberOfPlayers			|
--	| FK	Objective				|
--	| FK	Phase					|
--	| FK	Board					|
--	| 		CurrentRound			|
--	| 		IsOpen					|
--	| 		CardCounter				|
--	#####################################################
CREATE TABLE CurrentGames
(
	ID						BIGSERIAL,
	GameName				varchar(32),
	NumberOfJoinedPlayers	smallint,
	NumberOfPlayers			smallint,
	Objective				integer		REFERENCES Objectives(ID)	ON UPDATE CASCADE,
	Phase					integer		REFERENCES Phases(ID)	 	ON UPDATE CASCADE,
	Board					integer		REFERENCES Boards(ID) 		ON UPDATE CASCADE,
	CurrentRound			smallint,
	IsOpen					boolean,
	CardCounter				smallint,
	Version					bigint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------------------
--	|			Players				|
--	---------------------------------
--	| PK	ID						|
--	| FK	UserName				|
--	| FK	Color					|
--	| FK	Game					|
--	| 		isPlayedMove			|
--	| 		numberOfReinforcements	|
--	#####################################################
CREATE TABLE Players
(
	ID						SERIAL,
	UserId					bigint		REFERENCES Users(ID) 		ON UPDATE CASCADE,
	Game					bigint		REFERENCES CurrentGames(ID) ON UPDATE CASCADE,
	Color					integer		REFERENCES Colors(ID)	 	ON UPDATE CASCADE,
	isPlayedMove			boolean,
	numberOfReinforcements	integer,
	Version		bigint,
	
	PRIMARY KEY (ID)
);

CREATE INDEX gameIndex ON Players (Game);

--	#####################################################
--	---------------------
--	|		CardTypes	|
--	---------------------
--	| PK	ID			|
--	|   	Type		|
--	#####################################################
CREATE TABLE CardTypes
(
	ID		SERIAL,
	Type	varchar(32),
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|		Continents	|
--	---------------------
--	| PK	ID			|
--	|		Name		|
--	|		Bonus		|
--	#####################################################
CREATE TABLE Continents
(
	ID			SERIAL,
	Name		varchar(32),
	Bonus		smallint,
	
	PRIMARY KEY (ID)
);


--	#####################################################
--	---------------------
--	|		Fields		|
--	---------------------
--	| PK	ID			|
--	|		Name		|
--	|    FK	Continent	|
--	|		Version		|
--	#####################################################
CREATE TABLE Fields
(
	ID			SERIAL,
	Name		varchar(32),
	Continent	integer			REFERENCES Continents(ID),
	Version		bigint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|		Cards		|
--	---------------------
--	| PK	ID			|
--	|    FK	Type		|
--	|    FK	field		|
--	#####################################################
CREATE TABLE Cards
(
	ID		SERIAL,
	field	integer	REFERENCES Fields(ID)		ON UPDATE CASCADE,
	type	integer	REFERENCES CardTypes(ID)	ON UPDATE CASCADE,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	-------------------------
--	|		GameCards		|
--	-------------------------
--	| PK	ID				|
--	|    FK	game			|
--	|    FK	Player			|
--	|    FK	Card			|
--	|    	myRound			|
--	|    	version			|
--	#####################################################
CREATE TABLE GameCards
(
	ID		SERIAL,
	game	bigint		REFERENCES CurrentGames(ID)	ON UPDATE CASCADE,
	player	integer		REFERENCES Players(ID)		ON UPDATE CASCADE,
	card	integer		REFERENCES Cards(ID)		ON UPDATE CASCADE,
	myRound	smallint,
	Version	bigint,

	PRIMARY KEY (ID)
);

--	#####################################################
--	-------------------------
--	|		Connections		|
--	-------------------------
--	| PK 	ID				|
--	|    FK	Field1			|
--	|    FK	Field2			|
--	#####################################################
CREATE TABLE Connections
(
	ID		SERIAL,
	Field1	integer	REFERENCES Fields(ID) ON UPDATE CASCADE,
	Field2	integer	REFERENCES Fields(ID) ON UPDATE CASCADE,
	Version		bigint,
	
	CHECK (Field1<>Field2)
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
	Version		bigint,
	
	PRIMARY KEY (Board, Field)
);

--	#####################################################
--	---------------------------------
--	|	CompletedGamesStatistics	|
--	---------------------------------
--	| PK 	ID						|
--	|    FK	Player					|
--	|    	Ranking					|
--	#####################################################
CREATE TABLE CompletedGamesStatistics
(
	ID			SERIAL,
	Player		integer REFERENCES Players(ID) 		ON UPDATE CASCADE,
	Ranking		smallint,
	Version		bigint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------
--	|		Invites		|
--	---------------------
--	| PK 	ID			|
--	|    FK	Creator		|
--	|    FK	Game		|
--	|    FK	InvitedUser	|
--	#####################################################
CREATE TABLE Invites
(
	ID			SERIAL,
	Creator		bigint	REFERENCES Users(ID) 		ON UPDATE CASCADE,
	Game		bigint	REFERENCES CurrentGames(ID) ON UPDATE CASCADE,
	InvitedUser	integer	REFERENCES Users(ID)		ON UPDATE CASCADE,
	Version		bigint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	---------------------------------
--	|		TroopDeployments		|
--	---------------------------------
--	| PK	ID						|
--	|    FK	Game					|
--	|    FK	Field					|
--	|    FK	Player					|
--	| 		TroopNumber				|
--	| 		AddedTroops				|
--	#####################################################
CREATE TABLE TroopDeployments
(
	ID			SERIAL,
	Game		bigint	REFERENCES CurrentGames(ID)	ON UPDATE CASCADE,
	Field		integer	REFERENCES Fields(ID) 		ON UPDATE CASCADE,
	Player		integer	REFERENCES Players(ID) 		ON UPDATE CASCADE,
	TroopNumber	smallint,
	AddedTroops	smallint,
	Version		bigint,
	
	PRIMARY KEY (ID)
);

CREATE INDEX territoryIndex ON TroopDeployments(Field);

--	#####################################################
--	-------------------------
--	|		CommandTypes	|
--	-------------------------
--	| PK 	ID				|
--	| 		Name			|
--	#####################################################
CREATE TABLE CommandTypes
(
	ID		SERIAL,
	Name	varchar(32),
	Version		bigint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	-------------------------
--	|		Commands		|
--	-------------------------
--	| PK 	ID				|
--	|    FK	Game			|
--	|    FK	Phase			|
--	|   	Round			|
--	|    FK	Player			|
--	|    FK	Source			|
--	|    FK	Destination		|
--	|	 FK Type			|
--	| 		TroopNumber		|
--	#####################################################
CREATE TABLE Commands
(
	ID			SERIAL,
	Game		bigint	REFERENCES CurrentGames(ID)		ON UPDATE CASCADE,
	Phase		integer	REFERENCES Phases(ID)			ON UPDATE CASCADE,	
	Round		smallint,
	Player		integer	REFERENCES Players(ID) 			ON UPDATE CASCADE,
	Source		integer	REFERENCES TroopDeployments(ID)	ON UPDATE CASCADE,
	Destination	integer	REFERENCES TroopDeployments(ID)	ON UPDATE CASCADE,
	Type		integer	REFERENCES CommandTypes(ID) 	ON UPDATE CASCADE,
	TroopNumber	integer,
	Version		bigint,
	
	PRIMARY KEY (ID),
	CHECK (Source<>Destination)
);

--	#####################################################
--	-------------------------
--	|		AllianceTypes	|
--	-------------------------
--	| PK 	ID				|
--	| 		Name			|
--	#####################################################
CREATE TABLE AllianceTypes
(
	ID		SERIAL,
	Name	varchar(32),
	Version		bigint,
	
	PRIMARY KEY (ID)
);

--	#####################################################
--	-------------------------
--	|		Alliances		|
--	-------------------------
--	| PK 	ID				|
--	| 	 FK	Game			|
--	| 	 FK	Player1			|
--	| 	 FK	PLayer2			|
--	| 	 FK	Type			|
--	#####################################################
CREATE TABLE Alliances
(
	ID		SERIAL,
	Game	bigint	REFERENCES CurrentGames(ID) 	ON UPDATE CASCADE,
	Player1	integer	REFERENCES Players(ID) 			ON UPDATE CASCADE,
	Player2	integer	REFERENCES Players(ID) 			ON UPDATE CASCADE,
	Type	integer	REFERENCES AllianceTypes(ID) 	ON UPDATE CASCADE,
	Version		bigint,
	
	PRIMARY KEY (ID)
);