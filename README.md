## Optimisation des performances et de la maintenabilité du backend de Karuta.

Ceci est le dépôt associé au projet
[_Karuta_](https://github.com/karutaproject/karuta-backend).


### Contenu

-   `db` : les éléments de création de la base de données _DB_KARUTA_
-   `karuta-batch` : Le module contient les batches de l'application.
-   `karuta-business` : Le module contient le logique métier de l’application avec notamment les classes Manager.
-   `karuta-consumer` : Le module contient les éléments d'interaction avec la base de données. S'y trouvent notamment les DAO.
-   `karuta-model` : Le module contient les objets métiers (les beans DO). 
-   `karuta-webapp` : Le module contient "l’application web" du back-end (contrôleurs, Web services REST).Ces services sont destinées à être consommées par l’application avec IHM (karuta-frontend).
