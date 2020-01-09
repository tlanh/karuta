# Optimisation des performances et de la maintenabilité du backend de Karuta.

Ceci est le dépôt associé au projet
[_Karuta_](https://github.com/karutaproject/karuta-backend).


## Contenu

-   `db` : les éléments de création de la base de données _DB_KARUTA_
-   `karuta-batch` : Le module contient les batches de l'application.
-   `karuta-business` : Le module contient le logique métier de l’application avec notamment les classes Manager.
-   `karuta-consumer` : Le module contient les éléments d'interaction avec la base de données. S'y trouvent notamment les DAO.
-   `karuta-model` : Le module contient les objets métiers (les beans DO). 
-   `karuta-webapp` : Le module contient "l’application web" du back-end (contrôleurs, Web services REST).Ces services sont destinées à être consommées par l’application avec IHM (karuta-frontend).


## Procédure d’installation du back-end de Karuta.
Ces étapes vous permettront d’installer, de modifier et d’exécuter rapidement le back-end de Karuta.

### Dézipper le projet.
- Décompressez le fichier téléchargé, ce qui vous donnera un répertoire appelé karuta-master ou similaire.
- Déplacez-le dans votre zone de développement, par exemple. /devel/karuta/. NE choisissez PAS un répertoire dont le chemin contient des espaces, par exemple. n'utilisez pas de répertoire dont le chemin inclut C:/Documents and Settings/.

### Ouvrez-le dans Eclipse. 
- Eclipse 2019-12 R est préférable. Eclipse IDE pour les développeurs Java EE est un bon choix. (Le back-end de Karuta a été développé avec).
- Dans Eclipse, choisissez _File > Import..._, puis choisissez  _General > Existing Projects into Workspace_, cliquer sur _Next >_, définissez le répertoire racine sur votre répertoire karuta-master eg. /devel/karuta-master/, cliquer sur _Finish_.
- Le projet n’est pas encore construit, il affichera donc des erreurs.

### Installer JBoss Tools dans Eclipse. 
- Dans Eclipse, choisissez _Help > Eclipse Marketplace..._, puis dans la barre de recherche, tapez « JBoss Tools ». Une fois la recherche terminée, cliquez sur Install et suivez les instructions.

### Assurez-vous que le projet utilise Java 8.
Dans Eclipse, faites un clic droit sur le projet et choisissez Properties puis Java Compiler et assurer vous que Compiler compliance level is 1.8. Vous devrez peut-être activer Enable project specific settings.

### Actualisez le projet.
- Dans Eclipse, cliquez avec le bouton droit sur le projet racine « Karuta » et choisissez actualiser. Cela devrait générer le projet avec succès et ne montrer aucune erreur.
- Cela permet également d’exécuter un build maven, si le projet n’avait pas été encore construit jusque là. _NB : cela peut prendre du temps car des dépendances sont à télécharger_.

### Installer Apache Tomcat 9. 
Tomcat est un serveur HTTP à part entière qui gère les servlets et les JSP. Il sera notre serveur Web pour le développement.
- Go to [_Apache Downloads_](https://tomcat.apache.org/download-90.cgi) et téléchargez le .zip de la section Core.
- Une fois téléchargé, décompressez-le. Déplacez-le vers un emplacement approprié (par exemple, /devel/apache-tomcat-9.0.30).
- Dans Eclipse, en bas de la Fenêtre, dans la vue « Servers », cliquez sur New... et créer un nouveau serveur, suivez les instructions

### Installer MySQL. 
- Pour  Linux, suivez ce tutoriel : [_http://www-lisic.univ-littoral.fr/~ramat/downloads/tp-asr-5.pdf_](http://www-lisic.univ-littoral.fr/~ramat/downloads/tp-asr-5.pdf) .
- Pour Windows, installez [_EasyPHP_](https://www.easyphp.org/save-easyphp-devserver-latest.php). Durant l’installation, NE choisissez PAS le répertoire par défaut C:/Program Files/… Privilégier un répertoire utilisateur (cad. un répertoire ou pas besoin d’avoir des droits administrateur) comme par exemple : C:\Users\<Votre_Nom>\devel\EasyPhp

- Vous disposez désormais de l'environnement suivant : 

Web Server           | Business             | Persistence          | Database Server          | Logger
-------------------- | -------------------- | -------------------- | ------------------------ | --------- 
Tomcat 9             | Spring IOC           | Hibernate            | MySQL                    | Log4j
(As jars in project) | (As jars in project) | (As jars in project) | (Nécessite installation) | (As jars in project)

## Comment utiliser Karuta ?

### Démarrer Karuta dans Eclipse :
- Clic droit sur Tomcat Server 9 => Add and Remove « karuta-webapp ».
- Si erreur, notamment ClassNotFoundException for org.slf4j.Logger or org.slf4j.impl.StaticLoggerBinder,  vérifier que les arguments et le chemin de classe correspondent à ceux indiqués ci-dessus.
- Vérifiez que Karuta fonctionne en allant, avec votre navigateur web, sur la page [_http://localhost:8080/karuta-webapp_](http://localhost:8080/karuta-webapp). Une page avec It works doit s’afficher !

### Remplissez la base de données MySQL :
1. Dans Eclipse, arrêter Karuta-webapp (dans la vue Console, cliquez sur la case rouge).
2. À partir du projet racine « Karuta », ouvrir les dossiers db > karuta-backend. A l’intérieur du dossier se trouve les scripts MySQL que vous devrez exécuter.
3. Ouvrir phpMyAdmin
	- Sur Windows, d’abord démarrer EasyPhp puis démarrer le serveur de base de données et le serveur HTTP. À partir du dashboard d’EasyPHP, ouvrez phpMyAdmin.
	- Sur Linux, [_http://localhost/phpmyadmin/_](http://localhost/phpmyadmin/)
4. Pour créer la base de données, exécuter dans l'ordre les scripts SQL suivants :
	- `01_create_db_user_karuta.sql`
	- `02_create_db_karuta.sql`
	- `10_insert_data_db_ticket.sql`

### Redémarrez Karuta.
- Vérifiez que Karuta fonctionne en allant, avec votre navigateur web, sur la page [_http://localhost:8080/karuta-webapp_](http://localhost:8080/karuta-webapp)
	- Essayez de faire un test rapide pour voir si le système fonctionne ...
	- Dans Eclipse, projet karuta-webapp => recherchez le fichier de test `src/test/java/eportfolium/comkaruta/webapp/rest/controller/CredentialBasicLiveTest.java`
	- Double clic sur le fichier pour l’ouvrir.
Choisissez Run > Run As > Junit Test
Dans la vue « Console », le texte suivant doit s’afficher : _Output from Server .... created._
