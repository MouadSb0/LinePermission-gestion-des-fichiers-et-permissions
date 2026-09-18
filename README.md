# LinePermission - Partie 2

Application console Java : authentification (partie 1) et gestion des fichiers
avec permissions rwd (partie 2).

## Commandes

Authentification :
- `signup <login> <mot_de_passe>`
- `login <login> <mot_de_passe>`
- `logout`
- `exit`

Fichiers :
- `ls -l` : liste tous les fichiers et leurs droits
- `touch <fichier>` : cree un fichier vide (rwd|---)
- `cat <fichier>` : affiche le contenu (droit r)
- `nano <fichier>` : remplace le contenu jusqu'a une ligne EOF (droit w)
- `chmod <r|w|d> <fichier>` : donne un droit aux autres (proprietaire)
- `chmod -<r|w|d> <fichier>` : retire un droit aux autres (proprietaire)

## Lancer le jar

```
java -jar linepermission.jar
```

Le fichier `lib/jbcrypt-0.4.jar` doit rester a cote du jar (Class-Path du manifest).

## Statistiques

- stats : ouvre le menu d'analyse des logs (nombre d'actions, refus, utilisateurs, top fichiers...)
