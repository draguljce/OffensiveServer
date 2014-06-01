\connect offensive;

--	#####################################################
--	---------------------
--	|		UserType	|
--	---------------------
--	| PK	Name		|
--	#####################################################
INSERT INTO UserTypes VALUES ('OffensiveUser'), ('FacebookUser');

--	#####################################################
--	---------------------
--	|		Users		|
--	---------------------
--	| PK	ID			|
--	| FK	Type		|
--	#####################################################
INSERT INTO Users VALUES (1, 'FacebookUser');

--	#####################################################
--	---------------------
--	|	FacebookUser	|
--	---------------------
--	| PK	FacebookID	|
--	| 		userID		|
--	#####################################################
INSERT INTO FacebookUsers VALUES (1282639449, 1);


--	#####################################################
--	---------------------
--	|		Colors		|
--	---------------------
--	| PK	ID			|
--	|   	Name		|
--	#####################################################
INSERT INTO Colors (Name) VALUES ('Red'), ('Green'), ('Blue'), ('Yellow'), ('Black');

--	#####################################################
--	---------------------
--	|		Phases		|
--	---------------------
--	| PK	ID		|
--	| 		Name		|
--	#####################################################
INSERT INTO Phases (ID, Name) VALUES (0, 'Not started'), (1, 'Reinforcements'), (2, 'Attack'), (3, 'Battle'), (4, 'Move');

--	#####################################################
--	---------------------
--	|		Objectives	|
--	---------------------
--	| PK	Description	|
--	#####################################################
INSERT INTO Objectives VALUES (0, 'Conquer the world'), (1, 'Win 24 territories');

--	#####################################################
--	---------------------
--	|		CardTypes	|
--	---------------------
--	| PK	ID			|
--	|   	Type		|
--	#####################################################
INSERT INTO CardTypes VALUES (0, 'Soldier'), (1, 'Horse'), (2, 'Artilery');

--	#####################################################
--	-------------------------
--	|		AllianceTypes	|
--	-------------------------
--	| PK 	ID				|
--	| 		Name			|
--	#####################################################
INSERT INTO AllianceTypes (Name) VALUES ('Allied'), ('At war'), ('Offer');

--	#####################################################
--	---------------------
--	|		Fields		|
--	---------------------
--	| PK	ID			|
--	|		Name		|
--	#####################################################
INSERT INTO Fields VALUES 
(1, 'ALASKA'),(2, 'ALBERTA'), (3, 'CENTRAL_AMERICA'),
(4, 'EASTERN_US'), (5, 'GREENLAND'), (6, 'NORTHWEST_TERRITORY'),
(7, 'ONTARIO'), (8, 'QUEBEC'), (9, 'WESTERN_US'),
(10, 'ARGENTINA'), (11, 'BRAZIL'), (12, 'PERU'),
(13, 'VENEZUELA'), (14, 'GREAT_BRITAIN'), (15, 'ICELAND'),
(16, 'NORTHERN_EUROPE'), (17, 'SCANDINAVIA'), (18, 'SOUTHERN_EUROPE'),
(19, 'UKRAINE'), (20, 'WESTERN_EUROPE'), (21, 'CONGO'),
(22, 'EAST_AFRICA'), (23, 'EGYPT'), (24, 'MADAGASCAR'),
(25, 'NORTH_AFRICA'), (26, 'SOUTH_AFRICA'), (27, 'AFGHANISTAN'),
(28, 'CHINA'), (29, 'INDIA'), (30, 'IRKUTSK'),
(31, 'JAPAN'), (32, 'KAMCHATKA'), (33, 'MIDDLE_EAST'),
(34, 'MONGOLIA'), (35, 'SIAM'), (36, 'SIBERIA'),
(37, 'URAL'), (38, 'YAKUTSK'), (39, 'EASTERN_AUSTRALIA'),
(40, 'INDONESIA'), (41, 'NEW_GUINEA'), (42, 'WESTERN_AUSTRALIA');

--	#####################################################
--	---------------------
--	|		Boards		|
--	---------------------
--	| PK	ID			|
--	#####################################################
INSERT INTO Boards VALUES (1);

--	#####################################################
--	---------------------
--	|		HasFields	|
--	---------------------
--	| PK FK	Board		|
--	| PK FK	Field		|
--	#####################################################
INSERT INTO HasFields VALUES
(1, 1), (1, 2), (1, 3),
(1, 4), (1, 5), (1, 6),
(1, 7), (1, 8), (1, 9),
(1, 10), (1, 11), (1, 12),
(1, 13), (1, 14), (1, 15),
(1, 16), (1, 17), (1, 18),
(1, 19), (1, 20), (1, 21),
(1, 22), (1, 23), (1, 24),
(1, 25), (1, 26), (1, 27),
(1, 28), (1, 29), (1, 30),
(1, 31), (1, 32), (1, 33),
(1, 34), (1, 35), (1, 36),
(1, 37), (1, 38), (1, 39),
(1, 40), (1, 41), (1, 42);

--	#####################################################
--	-------------------------
--	|		Connections		|
--	-------------------------
--	| PK FK	Field1			|
--	| PK FK	Field2			|
--	#####################################################
INSERT INTO Connections VALUES
(1, 2), (1, 6), (2, 6),
(5, 6), (6, 7), (5, 8),
(5, 7), (2, 7), (7, 8),
(4, 7), (7, 9), (4, 9),
(3, 4), (3, 9), (3, 13),
(12, 13), (11, 13), (11, 12),
(10, 12), (10, 11), (11, 25),
(25, 21), (23, 25), (25, 22),
(21, 22), (21, 26), (22, 26),
(22, 24), (24, 26), 
(22, 23), (18, 23), (18, 25),
(20, 25), (18, 20), (14, 20),
(16, 20), (14, 15), (5, 15),
(15, 17), (14, 17), (14, 16),
(16, 18), (18, 19), (16, 17),
(16, 19), (17, 19), (18, 33),
(23, 33), (19, 33), (19, 27),
(19, 37), (33, 27), (33, 29),
(27, 29), (27, 28), (27, 37),
(36, 37), (28, 37), (36, 38),
(30, 36), (34, 36), (28, 36),
(32, 38), (30, 38), (1, 32),
(30, 32), (32, 34), (31, 32),
(30, 34), (31, 34), (28, 34),
(28, 29), (28, 35), (29, 35),
(35, 40), (40, 41), (40, 42),
(41, 42), (39, 41), (39, 42);