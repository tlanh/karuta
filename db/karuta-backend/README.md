# Base de données : karuta_backend

Voici les éléments relatifs à la base de données **karuta_backend**.



## Détail des fichiers

-   `01_create_db_user_karuta.sql` : script de création de l'utilisateur de la base de données
-   `02_create_db_karuta.sql` : script de création du schéma de la base de données
-   `10_insert_data_db_karuta.sql` : script d'ajout de données de démo dans la base de données
-   `50_clear_db_karuta.sql` : script de purge de la base de données :
    -   supprime toutes les données contenues dans les tables
    -   réinitialise les séquences
-   `MPD_db_karuta.jpg` : modèle physique de données de la base de données




## Procédures


### Création de la base de données

Exécuter dans l'ordre, les scripts SQL suivants :
1.  `01_create_db_user_karuta.sql`
2.  `02_create_db_karuta.sql`
3.  `10_insert_data_db_ticket.sql`


### Purge de la base de données

Exécuter le script SQL : `50_clear_db_karuta.sql`


### Réinitialiser les données de la base de données

Exécuter dans l'ordre, les scripts SQL suivants :
1.  `50_clear_db_karuta.sql`
2.  `10_insert_data_db_ticket.sql`

