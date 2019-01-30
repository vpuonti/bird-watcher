
========================================================================
Suomen 3. lintuatlaksen tulokset - Results of the 3rd Finnish bird atlas
========================================================================

Lue taustaa aineistoista ja käyttöoikeuksista osoitteessa http://atlas3.lintuatlas.fi/taustaa/kaytto
Lisätietoja voi kysyä osoitteesta www@luomus.fi.

Please read background information http://atlas3.lintuatlas.fi/english
For more information, contact www@luomus.fi.


--------------------------
atlas3-breeding-data.txt
--------------------------

Atlaksen varsinainen data: mitä lajeja pesii missäkin ruudussa.

speciesCode			Lajin numerokoodi
speciesAbbr			Lajin kuusikirjaiminen lyhenne
N					Yhtenäiskoordinaatiston peninkulmaruudun N-koordinaatti
E					Yhtenäiskoordinaatiston peninkulmaruudun E-koordinaatti
breedingIndex		Lajin suurin pesimävarmuusindeksi
breedingCategory	Lajin pesimävarmuusluokka

Pesimävarmuusluokat on koodattu seuraavasti:

0 = Ei havaintoa
1 = Epätodennäköinen pesintä
2 = Mahdollinen pesintä
3 = Todennäköinen pesintä
4 = Varma pesintä

Tiedostossa ei ole tietoja niistä lajeista, jotka lajiluettelossa on merkitty salatuiksi.


--------------------------
atlas3-grid-data.txt
--------------------------

Ruutujen selvitysasteet ja muut tiedot.

grid				Ruudun numerokoodi
N					Yhtenäiskoordinaatiston peninkulmaruudun N-koordinaatti
E					Yhtenäiskoordinaatiston peninkulmaruudun E-koordinaatti
regionNumber		Paikallisen lintutieteellisen yhdistyksen numerokoodi
societyNameFI		Paikallisen lintutieteellisen yhdistyksen nimi suomeksi
societyNameSV		Paikallisen lintutieteellisen yhdistyksen nimi ruotsiksi
municipality		Kunnan nimi (atlaksen aikaisen kuntajaottelun mukaan)
gridName			Ruudun nimi
level1				Ruudun pesimisvarmuussumman raja-arvo selvitysasteluokalle 1
level2				Ruudun pesimisvarmuussumman raja-arvo selvitysasteluokalle 2
level3				Ruudun pesimisvarmuussumman raja-arvo selvitysasteluokalle 3
level4				Ruudun pesimisvarmuussumman raja-arvo selvitysasteluokalle 4
level5				Ruudun pesimisvarmuussumman raja-arvo selvitysasteluokalle 5
activitySum			Ruudun painotettu pesimävarmuussumma
activityCategory	Ruudun selvitysasteluokka


Selvitysasteluokat on koodattu seuraavasti:

0 = Ei havaintoja
1 = Satunnaistietoja
2 = Välttävä
3 = Tyydyttävä
4 = Hyvä
5 = Erinomainen


--------------------------
species-names.txt
--------------------------

Atlaksessa mukana olevien lintulajien nimet.

speciesAbbr 		Lajin kuusikirjaiminen lyhenne
speciesSCI			Lajin tieteellinen nimi
speciesFI			Lajin suomenkielinen nimi
speciesSV			lajin ruotsinkielinen nimi
speciesEN			Lajin englanninkielinen nimi
visibility			Lajin tietojen julkisuus (0 = salattu, 1 = julkinen)

Salattavat lajit on valittu julkisuuslain 24 § mukaisesti. (Laki viranomaisten toiminnan julkisuudesta 21.5.1999/621)

