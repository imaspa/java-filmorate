MERGE INTO MPA_RATING (ID, NAME) VALUES (1, 'G'),
                                        (2, 'PG'),
                                        (3, 'PG-13'),
                                        (4, 'R'),
                                        (5, 'NC-17');

MERGE INTO GENRE (ID, NAME) VALUES (1, 'Комедия'),
                                   (2, 'Драма'),
                                   (3, 'Мультфильм'),
                                   (4, 'Триллер'),
                                   (5, 'Документальный'),
                                   (6, 'Боевик');