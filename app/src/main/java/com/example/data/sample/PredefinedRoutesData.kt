package com.example.data.sample

import com.example.ui.settings.LanguageMode

object PredefinedRoutesData {

    val ROUTES: List<PredefinedRouteInfo> = listOf(
        // 1. Camino Francés (últimos 100 km)
        PredefinedRouteInfo(
            title = "Camino Francés (últimos 100 km)",
            subtitle = "Los últimos 100 km de Sarria a Santiago de Compostela",
            totalKm = 114.2,
            note = null,
            stages = listOf(
                PredefinedStageInfo(
                    dayNumber = 1,
                    title = "Etapa 1: Sarria a Portomarín",
                    origin = "Sarria",
                    destination = "Portomarín",
                    distanceKm = 22.2,
                    elevationGainM = 350,
                    elevationLossM = 420,
                    estimatedTime = "5h 30m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Inicio tradicional de los últimos 100 kilómetros para obtener la Compostela. Salida por la rúa Maior de Sarria pasando junto al convento de la Magdalena y la iglesia de Santa Marina. Es obligatorio sellar la credencial al menos dos veces al día en territorio gallego; puedes hacerlo en la iglesia románica de Santiago en Barbadelo (siglo XII) y en los pequeños albergues de Rente, Brea o Ferreiros. Disfruta de las aldeas ganaderas y cruza el puente sobre el embalse de Belesar para entrar a Portomarín. En Portomarín visita la iglesia-fortaleza de San Nicolás, trasladada piedra a piedra cuando se anegó el viejo pueblo. La mejor hora para salir es sobre las 7:30 h para caminar con calma.\nCONSEJOS DE ÚLTIMA HORA: Los peregrinos en páginas y foros especializados señalan gran afluencia a primera hora de la mañana. En días de lluvia, las corredoiras tradicionales de tierra suelen acumular barro en As Rozas y A Brea. Conviene llevar calzado con buen agarre y bastones para el descenso pronunciado hacia el embalse de Belesar."
                ),
                PredefinedStageInfo(
                    dayNumber = 2,
                    title = "Etapa 2: Portomarín a Palas de Rei",
                    origin = "Portomarín",
                    destination = "Palas de Rei",
                    distanceKm = 24.8,
                    elevationGainM = 450,
                    elevationLossM = 320,
                    estimatedTime = "6h 00m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Salida cruzando la pasarela sobre el río Miño y ascenso continuado hacia la Sierra de Ligonde. A la altura de Castromaior es casi obligatorio desviarse apenas 400 metros para visitar el castro prerromano de la Edad del Hierro, con vistas panorámicas excepcionales. En Ligonde admira el cruceiro de Lameiros del siglo XVII (con relieves de la Pasión y la Maternidad) y el enclave del antiguo hospital de peregrinos donde descansaron Carlos V y Felipe II. Paradas recomendadas para sellar y reponer fuerzas en Gonzar, Hospital da Cruz o Ventas de Narón. En Palas de Rei no dejes de sellar en la iglesia de San Tirso.\nCONSEJOS DE ÚLTIMA HORA: Subida tendida pero constante de unos 8 kilómetros en el primer tercio de la etapa. En foros de peregrinos se advierte que los tramos de monte abierto entre Gonzar y Ventas de Narón carecen de sombra; en verano es indispensable salir temprano, llevar sombrero y cargar agua fresca en Portomarín."
                ),
                PredefinedStageInfo(
                    dayNumber = 3,
                    title = "Etapa 3: Palas de Rei a Arzúa",
                    origin = "Palas de Rei",
                    destination = "Arzúa",
                    distanceKm = 28.5,
                    elevationGainM = 410,
                    elevationLossM = 480,
                    estimatedTime = "6h 45m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Cruce del límite provincial de Lugo hacia A Coruña en O Coto. En Leboreiro detente ante la iglesia de Santa María y cruza el puente medieval sobre el río Seco con su calzada romana. Parada gastronómica y cultural indiscutible en Melide: degustar el pulpo á feira en sus pulperías tradicionales y sellar en la iglesia de San Pedro o en la capilla de San Roque junto a su cruceiro del siglo XIV. En Melide se produce la unión histórica con el Camino Primitivo. La etapa concluye en Arzúa, famosa por su queso con Denominación de Origen Arzúa-Ulloa y la iglesia de Santiago.\nCONSEJOS DE ÚLTIMA HORA: Considerada una etapa rompepiernas por su continuo perfil de toboganes con subidas y bajadas cortas pero pronunciadas por corredoiras pedregosas, como el cruce del río Catasol y el arroyo de Boente. Los foros recomiendan dosificar el esfuerzo, usar bastones y extremar la precaución al cruzar la carretera nacional N-547."
                ),
                PredefinedStageInfo(
                    dayNumber = 4,
                    title = "Etapa 4: Arzúa a O Pedrouzo",
                    origin = "Arzúa",
                    destination = "O Pedrouzo",
                    distanceKm = 19.3,
                    elevationGainM = 280,
                    elevationLossM = 320,
                    estimatedTime = "4h 45m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Jornada tranquila y de perfil dócil que discurre entre centenarios robledales (carballeiras), prados verdes y frondosos bosques de eucaliptos. Pueblos pequeños y apacibles como Salceda, Brea, Calzada y Santa Irene, donde se encuentra la entrañable ermita barroca de Santa Irene y su fuente de aguas supuestamente curativas donde es costumbre sellar. Llegada a O Pedrouzo (Arca do Pino), capital del municipio de O Pino, donde se respira un animado ambiente de víspera de meta entre peregrinos de todo el mundo.\nCONSEJOS DE ÚLTIMA HORA: Etapa con múltiples cruces de la transitada carretera N-547. En páginas y foros especializados se insiste en utilizar siempre los pasos subterráneos y pasos señalizados para peatones. En temporada alta los albergues de O Pedrouzo se llenan pronto; conviene reservar o iniciar la caminata antes de las 7:30 h."
                ),
                PredefinedStageInfo(
                    dayNumber = 5,
                    title = "Etapa 5: O Pedrouzo a Santiago de Compostela",
                    origin = "O Pedrouzo",
                    destination = "Santiago de Compostela",
                    distanceKm = 19.4,
                    elevationGainM = 310,
                    elevationLossM = 350,
                    estimatedTime = "4h 45m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: La etapa cumbre de la peregrinación. Salida al amanecer atravesando los bosques de Amenal hacia Lavacolla, donde los romeros medievales se aseaban en el arroyo antes de entrar en la ciudad del Apóstol. Ascenso al Monte do Gozo para divisar con emoción las torres de la Catedral de Santiago. Entrada a pie por el barrio de San Lázaro, rúa das Fontiñas, Porta do Camiño, rúa das Ánimas, plaza de Cervantes y bajada por la Acibechería para entrar al Obradoiro bajo el arco del Pazo de Raxoi con el eco de las gaitas gallegas. Sellar la credencial en la Oficina del Peregrino en rúa Carretas.\nCONSEJOS DE ÚLTIMA HORA: Si deseas asistir a la Misa del Peregrino de las 12:00 h en la Catedral, sal de O Pedrouzo a más tardar a las 6:30 h. En el Monte do Gozo desvíate 200 metros hacia el monumento a los peregrinos para obtener la mejor vista panorámica de Santiago."
                )
            )
        ),

        // 2. Camino Francés Completo
        PredefinedRouteInfo(
            title = "Camino Francés",
            subtitle = "De Saint-Jean-Pied-de-Port a Santiago por la ruta jacobea histórica",
            totalKm = 779.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(
                    dayNumber = 1,
                    title = "Etapa 1: Saint-Jean-Pied-de-Port a Roncesvalles",
                    origin = "Saint-Jean-Pied-de-Port",
                    destination = "Roncesvalles",
                    distanceKm = 25.1,
                    elevationGainM = 1250,
                    elevationLossM = 480,
                    estimatedTime = "7h 30m",
                    difficulty = "Muy exigente",
                    notes = "RECOMENDACIONES: Salida por la Puerta de España de Saint-Jean y ascenso por la mítica Ruta de Napoleón cruzando el Collado de Bentartea y Lepoeder (1.430 m). Llegada a la Real Colegiata de Roncesvalles, capilla de Sancti Spiritus (Silo de Carlomagno) e iglesia de Santiago. Imprescindible sellar en la acogida de la Colegiata y asistir a la bendición vespertina de peregrinos.\nCONSEJOS DE ÚLTIMA HORA: Etapa de alta montaña con 1.250 m de desnivel positivo. Si hay niebla, nieve o mal tiempo entre noviembre y marzo, la Ruta de Napoleón está cerrada por decreto foral; es obligatorio tomar la variante por Valcarlos. Llevar mínimo 2 litros de agua y comida energética; no hay servicios tras Orisson."
                ),
                PredefinedStageInfo(
                    dayNumber = 2,
                    title = "Etapa 2: Roncesvalles a Zubiri",
                    origin = "Roncesvalles",
                    destination = "Zubiri",
                    distanceKm = 21.4,
                    elevationGainM = 290,
                    elevationLossM = 750,
                    estimatedTime = "5h 45m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Descenso entre bosques de hayas y robles navarros por los valles de Erro y Esteribar. Parada en Auritz/Burguete y Espinal con sus caseríos tradicionales. Ascenso al Alto de Mezkiritz (estela de la Virgen del Camino) y al Alto de Erro. Llegada a Zubiri cruzando el legendario Puente de la Rabia sobre el río Arga.\nCONSEJOS DE ÚLTIMA HORA: La bajada final desde el Alto de Erro hasta Zubiri es empinada, pedregosa y muy irregular; en páginas y foros especializados se advierte que con humedad las lajas de pizarra mojadas resbalan peligrosamente. Bastones imprescindibles."
                ),
                PredefinedStageInfo(
                    dayNumber = 3,
                    title = "Etapa 3: Zubiri a Pamplona",
                    origin = "Zubiri",
                    destination = "Pamplona",
                    distanceKm = 20.4,
                    elevationGainM = 210,
                    elevationLossM = 310,
                    estimatedTime = "5h 00m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Caminata siguiendo el curso del río Arga por senderos arbolados y puentes medievales como Larrasoaña e Irotz. En Trinidad de Arre cruzar el puente gótico y ermita sobre el río Ulzama. Entrada monumental a Pamplona por el puente de la Magdalena, Portal de Francia y murallas renacentistas. Visitar la Catedral de Santa María y la Plaza del Castillo.\nCONSEJOS DE ÚLTIMA HORA: Etapa suave y con buen firme. En foros se recomienda tomar la variante del paseo fluvial del Arga a la altura de Burlada si se prefiere un acceso verde y tranquilo a la ciudad sin asfalto."
                ),
                PredefinedStageInfo(
                    dayNumber = 4,
                    title = "Etapa 4: Pamplona a Puente la Reina",
                    origin = "Pamplona",
                    destination = "Puente la Reina",
                    distanceKm = 23.9,
                    elevationGainM = 430,
                    elevationLossM = 490,
                    estimatedTime = "5h 45m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Salida atravesando la Ciudadela y ascenso al emblemático Alto del Perdón (770 m), con el célebre monumento escultórico donde 'se cruza el camino del viento con el de las estrellas'. En Puente la Reina, admirar la iglesia del Crucifijo, la iglesia de Santiago y cruzar el magnífico puente románico sobre el río Arga donde confluyen el Camino Francés y el Aragonés.\nCONSEJOS DE ÚLTIMA HORA: El descenso desde el Alto del Perdón hacia Uterga es muy pedregoso y con cantos rodados sueltos. Bajar con paso firme y bastones para evitar torceduras de tobillo comentadas con frecuencia en foros."
                ),
                PredefinedStageInfo(
                    dayNumber = 5,
                    title = "Etapa 5: Puente la Reina a Estella",
                    origin = "Puente la Reina",
                    destination = "Estella",
                    distanceKm = 21.6,
                    elevationGainM = 380,
                    elevationLossM = 350,
                    estimatedTime = "5h 15m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Paso por Cirauqui, pueblo medieval encaramado a un cerro con trazado concéntrico y calzada romana bien conservada. Cruce de los puentes medievales de Lorca y Villatuerta. En Estella-Lizarra, la 'Toledo del Norte', visitar la iglesia de San Pedro de la Rúa, el Palacio de los Reyes de Navarra y el convento de Santo Domingo.\nCONSEJOS DE ÚLTIMA HORA: Jornada con repechos cortos pero continuos. Hay fuentes regulares en Mañeru y Cirauqui. En verano salir a primera hora para evitar las horas centrales bajo el sol en los campos abiertos de cereal."
                ),
                PredefinedStageInfo(
                    dayNumber = 6,
                    title = "Etapa 6: Estella a Los Arcos",
                    origin = "Estella",
                    destination = "Los Arcos",
                    distanceKm = 21.3,
                    elevationGainM = 290,
                    elevationLossM = 310,
                    estimatedTime = "5h 00m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: A la salida de Estella parada obligatoria en el Monasterio de Santa María la Real de Irache y su célebre Bodega con la Fuente del Vino y del Agua, donde se puede sellar y catar con moderación. Pueblos como Azqueta y Villamayor de Monjardín (castillo de San Esteban). En Los Arcos, impresionante iglesia de Santa María con su coro y torre barroca.\nCONSEJOS DE ÚLTIMA HORA: Entre Villamayor de Monjardín y Los Arcos hay un tramo de 12 kilómetros completamente desierto sin sombras ni puntos de abastecimiento. Cargar suficiente agua y provisiones antes de salir de Villamayor."
                ),
                PredefinedStageInfo(
                    dayNumber = 7,
                    title = "Etapa 7: Los Arcos a Logroño",
                    origin = "Los Arcos",
                    destination = "Logroño",
                    distanceKm = 27.6,
                    elevationGainM = 390,
                    elevationLossM = 460,
                    estimatedTime = "6h 30m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Paso por Sansol y Torres del Río, joya octogonal del románico de inspiración templaria (iglesia del Santo Sepulcro). En Viana, visitar las ruinas góticas de San Pedro donde reposan los restos de César Borgia. Cruce del río Ebro por el Puente de Piedra y entrada a Logroño, capital de La Rioja; tapeo en la calle Laurel y concatedral de Santa María de la Redonda.\nCONSEJOS DE ÚLTIMA HORA: Camino de continuas cuestas entre viñedos y pistas de grava. El tramo final de entrada a Logroño bordeando la carretera nacional puede resultar caluroso en verano; aprovechar el parque del Ebro para un acceso más fresco."
                ),
                PredefinedStageInfo(
                    dayNumber = 8,
                    title = "Etapa 8: Logroño a Nájera",
                    origin = "Logroño",
                    destination = "Nájera",
                    distanceKm = 29.0,
                    elevationGainM = 410,
                    elevationLossM = 360,
                    estimatedTime = "6h 45m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Salida por el agradable parque y embalse de La Grajera. Paisaje ondulado rodeado de viñedos de la D.O.Ca. Rioja. Parada en Navarrete para admirar su alfarería artesanal, la iglesia de la Asunción y los restos del antiguo hospital de San Juan de Acre. Entrada a Nájera y visita al Monasterio de Santa María la Real con el panteón de los reyes navarros y claustro de los Caballeros.\nCONSEJOS DE ÚLTIMA HORA: Etapa larga de casi 29 km. El ascenso desde La Grajera al Alto de San Antón es tendido pero exigente a pleno sol. Entre Navarrete y Ventosa hay tramos de sendero pedregoso y pistas paralelas a la autovía; seguir con atención las flechas amarillas."
                ),
                PredefinedStageInfo(
                    dayNumber = 9,
                    title = "Etapa 9: Nájera a Santo Domingo de la Calzada",
                    origin = "Nájera",
                    destination = "Santo Domingo de la Calzada",
                    distanceKm = 20.7,
                    elevationGainM = 320,
                    elevationLossM = 210,
                    estimatedTime = "5h 00m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Caminata entre viñedos y campos de cereal pasando por Azofra y su rollo jurisdiccional. En Santo Domingo de la Calzada, villa jacobea fundada por el santo constructor para el peregrino, visitar la Catedral con el célebre gallinero gótico donde cantan el gallo y la gallina vivos en recuerdo del milagro legendario. Sellar en la catedral.\nCONSEJOS DE ÚLTIMA HORA: Etapa cómoda y de perfil suave sin grandes desniveles. Caminar por pistas de tierra en buen estado. Hidratarse bien en los meses de calor pues los tramos entre Azofra y Cirueña ofrecen poca vegetación de sombra."
                ),
                PredefinedStageInfo(
                    dayNumber = 10,
                    title = "Etapa 10: Santo Domingo de la Calzada a Belorado",
                    origin = "Santo Domingo de la Calzada",
                    destination = "Belorado",
                    distanceKm = 22.0,
                    elevationGainM = 280,
                    elevationLossM = 190,
                    estimatedTime = "5h 15m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Despedida de La Rioja en Grañón, con su hospitalario albergue parroquial de San Juan Bautista donde se comparte cena comunitaria. Entrada en la comunidad de Castilla y León por Redecilla del Camino (iglesia con valiosa pila bautismal románica del siglo XII). En Belorado, pasear por su Plaza Mayor porticada y la iglesia de Santa María.\nCONSEJOS DE ÚLTIMA HORA: Terreno llano de transición hacia la meseta norte por caminos de concentración parcelaria paralelos a la carretera N-120. Extremar precauciones en los cruces señalizados de la carretera nacional."
                ),
                PredefinedStageInfo(
                    dayNumber = 11,
                    title = "Etapa 11: Belorado a San Juan de Ortega",
                    origin = "Belorado",
                    destination = "San Juan de Ortega",
                    distanceKm = 23.9,
                    elevationGainM = 420,
                    elevationLossM = 180,
                    estimatedTime = "5h 45m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Paso por Tosantos y su ermita rupestre de la Virgen de la Peña excavada en roca viva. Ascenso progresivo hacia los Montes de Oca atravesando Villambistia y Villafranca Montes de Oca. Llegada al apacible monasterio de San Juan de Ortega; en su iglesia románica admirar el capitel de la Anunciación que protagoniza el milagro de la luz en los equinoccios.\nCONSEJOS DE ÚLTIMA HORA: El ascenso por los Montes de Oca entre Villafranca y San Juan de Ortega (unos 12 km de bosque deshabitado) no tiene fuentes ni tiendas; cargar provisiones en Villafranca. Con lluvia el suelo arcilloso genera fango pegajoso."
                ),
                PredefinedStageInfo(
                    dayNumber = 12,
                    title = "Etapa 12: San Juan de Ortega a Burgos",
                    origin = "San Juan de Ortega",
                    destination = "Burgos",
                    distanceKm = 25.8,
                    elevationGainM = 190,
                    elevationLossM = 310,
                    estimatedTime = "6h 00m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Paso por Agés y el enclave prehistórico de Atapuerca, Patrimonio de la Humanidad. Vistas desde la cruz del monte hacia la cuenca de Burgos. Entrada a Burgos por la variante fluvial del río Arlanzón. En Burgos contemplar la magnífica Catedral gótica de Santa María (siglo XIII), el Arco de Santa María y las Huelgas. Sellar en el albergue municipal o en la catedral.\nCONSEJOS DE ÚLTIMA HORA: En páginas y foros especializados se recomienda firmemente tomar la variante de Castañares por el parque fluvial del Arlanzón para entrar a Burgos, evitando caminar más de 6 km entre naves industriales y tráfico de Gamonal."
                ),
                PredefinedStageInfo(
                    dayNumber = 13,
                    title = "Etapa 13: Burgos a Hornillos del Camino",
                    origin = "Burgos",
                    destination = "Hornillos del Camino",
                    distanceKm = 21.0,
                    elevationGainM = 220,
                    elevationLossM = 210,
                    estimatedTime = "5h 00m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Comienzo de la gran meseta castellana y sus horizontes infinitos de cereal. Salida por el puente de Malatos y el hospital del Rey. Paso por Tardajos y Rabé de las Calzadas, donde en el convento de las Clarisas se suele entregar una medalla bendecida. Descenso final por el páramo hacia el pueblo lineal de Hornillos del Camino.\nCONSEJOS DE ÚLTIMA HORA: La meseta impone su clima continental extremo: frío y viento matinal en primavera y otoño, y sol intenso en verano. Salir temprano, usar protección solar y llevar abundante agua ya que entre Rabé y Hornillos hay 8 km sin fuentes operativas."
                ),
                PredefinedStageInfo(
                    dayNumber = 14,
                    title = "Etapa 14: Hornillos del Camino a Castrojeriz",
                    origin = "Hornillos del Camino",
                    destination = "Castrojeriz",
                    distanceKm = 20.1,
                    elevationGainM = 190,
                    elevationLossM = 230,
                    estimatedTime = "4h 45m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Caminata solitaria y profunda por el páramo burgalés. Paso por el albergue de San Bol, oasis jacobeo en medio de la llanura. Cruce mágico bajo los arcos góticos del Convento en ruinas de San Antón, donde los monjes protegían a los peregrinos del fuego sagrado con la cruz tau. Llegada a Castrojeriz, presidida por su castillo en la cresta del cerro y la colegiata de Nuestra Señora del Manzano.\nCONSEJOS DE ÚLTIMA HORA: Jornada muy expuesta al viento y al sol sin sombras intermedias. Cargar agua en Hornillos. El paso bajo los arcos de San Antón se hace pegado a la calzada; caminar con precaución por el arcén."
                ),
                PredefinedStageInfo(
                    dayNumber = 15,
                    title = "Etapa 15: Castrojeriz a Frómista",
                    origin = "Castrojeriz",
                    destination = "Frómista",
                    distanceKm = 24.7,
                    elevationGainM = 280,
                    elevationLossM = 310,
                    estimatedTime = "5h 45m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: El gran reto del día es el ascenso al temido Alto de Mostelares (860 m), con una rampa del 12% que regala unas vistas inolvidables de la meseta. Cruce del río Pisuerga por el puente medieval de Itero de la Vega (Ponte Fitero) entrando en Palencia; ermita de San Nicolás. En Boadilla del Camino contemplar el rollo jurisdiccional gótico del siglo XV. Tramo final bordeando el Canal de Castilla y sus esclusas hasta Frómista, con la joya románica de San Martín de Tours (siglo XI).\nCONSEJOS DE ÚLTIMA HORA: El descenso de Mostelares es muy pronunciado sobre pista de hormigón y piedras sueltas; clavar bien los bastones. El tramo llano junto al Canal de Castilla es fresco y llano pero largo; hidratarse con regularidad."
                ),
                PredefinedStageInfo(
                    dayNumber = 16,
                    title = "Etapa 16: Frómista a Carrión de los Condes",
                    origin = "Frómista",
                    destination = "Carrión de los Condes",
                    distanceKm = 18.8,
                    elevationGainM = 110,
                    elevationLossM = 80,
                    estimatedTime = "4h 30m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Caminata por la llanura de Tierra de Campos siguiendo la senda paralela a la carretera comarcal. Población de Campos (ermita de San Miguel), Revenga y Villovieco. Llegada a Carrión de los Condes, villa medieval con el Monasterio de San Zoilo (claustro plateresco magistral), iglesia de Santa María del Camino y la portada románica de Santiago.\nCONSEJOS DE ÚLTIMA HORA: Etapa plana y rectilínea. Para evitar la monotonía del andadero paralelo a la carretera, en Población de Campos se sugiere tomar la variante que bordea el río Ucieza hacia Villovieco, más fresca y con mayor vegetación según reportan en foros."
                ),
                PredefinedStageInfo(
                    dayNumber = 17,
                    title = "Etapa 17: Carrión de los Condes a Ledigos",
                    origin = "Carrión de los Condes",
                    destination = "Ledigos",
                    distanceKm = 23.4,
                    elevationGainM = 140,
                    elevationLossM = 90,
                    estimatedTime = "5h 30m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: El desafío interior de la meseta: la histórica Vía Aquitana o Calzada de los Peregrinos. Salida desde Carrión pasando por el Real Monasterio de Santa Clara. Tramo continuo de casi 17 kilómetros sin ningún pueblo intermedio hasta Calzadilla de la Cueza, donde se reponen fuerzas. Continuación hacia Ledigos, pueblo típico de adobe donde la iglesia está consagrada a San Martín de Tours.\nCONSEJOS DE ÚLTIMA HORA: Es imprescindible salir de Carrión con agua abundante (mínimo 2 litros) y comida, pues no hay servicios entre Carrión y Calzadilla de la Cueza. Pista de grava y tierra sin sombras; en días calurosos salir antes de las 6:30 h."
                ),
                PredefinedStageInfo(
                    dayNumber = 18,
                    title = "Etapa 18: Ledigos a Sahagún",
                    origin = "Ledigos",
                    destination = "Sahagún",
                    distanceKm = 16.2,
                    elevationGainM = 90,
                    elevationLossM = 110,
                    estimatedTime = "4h 00m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Paso por Terradillos de los Templarios, pueblo vinculado a leyendas de la orden del Temple. Cruce de Moratinos y San Nicolás del Real Camino. En el mojón divisorio se abandona Palencia y se entra en la provincia de León. Cruce del santuario de la Virgen del Puente. Entrada a Sahagún, capital del mudéjar leonés: iglesia de San Tirso, iglesia de San Lorenzo y arco de San Benito.\nCONSEJOS DE ÚLTIMA HORA: Etapa corta y suave con pueblos regulares cada 4 o 5 kilómetros para desayunar o descansar. Buen firme de sendero de tierra compactada. Sahagún cuenta con todos los servicios y albergues bien equipados."
                ),
                PredefinedStageInfo(
                    dayNumber = 19,
                    title = "Etapa 19: Sahagún a El Burgo Ranero",
                    origin = "Sahagún",
                    destination = "El Burgo Ranero",
                    distanceKm = 17.7,
                    elevationGainM = 110,
                    elevationLossM = 90,
                    estimatedTime = "4h 15m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Salida cruzando el río Cea por el puente Canto. En Calzada del Coto se presenta la bifurcación histórica: la senda tradicional de los peregrinos hacia El Burgo Ranero o la variante por Calzadilla de los Hermanillos. Hacia El Burgo Ranero se camina por la 'Senda del Peregrino', vía protegida con hileras de árboles plantados para dar sombra. En El Burgo Ranero, visitar la laguna natural y su albergue municipal.\nCONSEJOS DE ÚLTIMA HORA: La senda arbolada es cómoda pero monótona y arenosa en algunos puntos. Aunque hay bancos y árboles jóvenes, las fuentes pueden estar secas en verano; asegurar agua en Bercianos del Real Camino."
                ),
                PredefinedStageInfo(
                    dayNumber = 20,
                    title = "Etapa 20: El Burgo Ranero a Mansilla de las Mulas",
                    origin = "El Burgo Ranero",
                    destination = "Mansilla de las Mulas",
                    distanceKm = 18.8,
                    elevationGainM = 80,
                    elevationLossM = 140,
                    estimatedTime = "4h 30m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Continuación por la senda de los árboles pasando por Reliegos, célebre por sus bodegas subterráneas excavadas en barro arcilloso y el ambiente fraternal de sus bares jacobeos. Llegada a Mansilla de las Mulas, villa medieval amurallada que conserva lienzos de murallas de canto rodado, la puerta del Castillo y el puente de piedra sobre el río Esla. Visitar el Museo Etnográfico Provincial.\nCONSEJOS DE ÚLTIMA HORA: Etapa llana y de perfil dócil. Se camina principalmente por pista de tierra paralela a la carretera local. Atención al cruzar los accesos de las vías rápidas próximas a Mansilla."
                ),
                PredefinedStageInfo(
                    dayNumber = 21,
                    title = "Etapa 21: Mansilla de las Mulas a León",
                    origin = "Mansilla de las Mulas",
                    destination = "León",
                    distanceKm = 18.5,
                    elevationGainM = 150,
                    elevationLossM = 110,
                    estimatedTime = "4h 30m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Salida cruzando el río Esla. Paso por Villarente y su largo puente de 18 arcos sobre el río Porma, y ascenso al Portillo para divisar por primera vez la ciudad de León. Entrada monumental hacia la excelsa Catedral gótica de Santa María de Regla (la Pulchra Leonina con sus vidrieras medievales), la Colegiata románica de San Isidoro (Panteón Real) y la Casa Botines de Gaudí. Barrio Húmedo para tapear.\nCONSEJOS DE ÚLTIMA HORA: La entrada a León por Arcahueja y la pasarela sobre la ronda este y polígonos industriales puede ser dura para los pies por el pavimento de asfalto continuo. Tomar con calma los últimos 6 kilómetros urbanos."
                ),
                PredefinedStageInfo(
                    dayNumber = 22,
                    title = "Etapa 22: León a San Martín del Camino",
                    origin = "León",
                    destination = "San Martín del Camino",
                    distanceKm = 24.6,
                    elevationGainM = 130,
                    elevationLossM = 110,
                    estimatedTime = "5h 45m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Salida frente al Parador de San Marcos y cruce del río Bernesga. Ascenso suave hacia La Virgen del Camino, visitando el santuario mariano con esculturas de Subirachs. Continuación por el páramo leonés hacia Valverde de la Virgen y San Miguel del Camino. Llegada a San Martín del Camino o bifurcación hacia Villar de Mazarife.\nCONSEJOS DE ÚLTIMA HORA: El trazado por San Martín discurre pegado a la N-120 con ruido de tráfico; en páginas y foros especializados muchos peregrinos recomiendan la variante sur por Villar de Mazarife si se busca mayor silencio y entorno puramente rural."
                ),
                PredefinedStageInfo(
                    dayNumber = 23,
                    title = "Etapa 23: San Martín del Camino a Astorga",
                    origin = "San Martín del Camino",
                    destination = "Astorga",
                    distanceKm = 23.7,
                    elevationGainM = 180,
                    elevationLossM = 130,
                    estimatedTime = "5h 30m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Llegada a Hospital de Órbigo y cruce de su soberbio Puente del Paso Honroso sobre el río Órbigo, escenario de las famosas justas caballerescas de Don Suero de Quiñones en 1434. Ascenso hacia el crucero de Santo Toribio con panorámica abierta a los Montes de León. Entrada a Astorga: murallas romanas, Catedral de Santa María y Palacio Episcopal neogótico de Antoni Gaudí. Degustar cocido maragato y chocolate artesano.\nCONSEJOS DE ÚLTIMA HORA: Subida pedregosa desde Santibáñez de Valdeiglesias hacia la meseta de Santo Toribio. Se recomienda una parada en el oasis de descanso de la Casa de los Dioses si está activo para tomar fruta fresca y agua."
                ),
                PredefinedStageInfo(
                    dayNumber = 24,
                    title = "Etapa 24: Astorga a Rabanal del Camino",
                    origin = "Astorga",
                    destination = "Rabanal del Camino",
                    distanceKm = 20.1,
                    elevationGainM = 310,
                    elevationLossM = 40,
                    estimatedTime = "5h 00m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Entrada en la comarca de la Maragatería. Pueblos con encanto de arquitectura de piedra rojiza y grandes portones como Murias de Rechivaldo, Castrillo de los Polvazares (desvío de 1 km muy recomendable), Santa Catalina de Somoza y El Ganso. Llegada a Rabanal del Camino, enclave jacobeo con el monasterio benedictino de San Salvador del Monte Irago donde se celebran vísperas cantadas en gregoriano.\nCONSEJOS DE ÚLTIMA HORA: Ascenso suave pero continuo de 870 a 1.150 metros de altitud. El clima refresca notablemente; guardar ropa de abrigo en la mochila incluso en verano. Pistas de tierra pedregosa en buen estado."
                ),
                PredefinedStageInfo(
                    dayNumber = 25,
                    title = "Etapa 25: Rabanal del Camino a Ponferrada",
                    origin = "Rabanal del Camino",
                    destination = "Ponferrada",
                    distanceKm = 32.2,
                    elevationGainM = 450,
                    elevationLossM = 980,
                    estimatedTime = "7h 45m",
                    difficulty = "Exigente",
                    notes = "RECOMENDACIONES: Jornada épica del Camino Francés. Ascenso al pueblo resucitado de Foncebadón y coronación de la Cruz de Ferro (1.505 m), donde la tradición manda depositar una piedra traída del lugar de origen. Descenso vertiginoso hacia El Bierzo pasando por El Acebo (balconadas de madera y pizarra), Riego de Ambrós y Molinaseca (puente de los Peregrinos sobre el río Meruelo). Entrada a Ponferrada y visita a su colosal Castillo Templario.\nCONSEJOS DE ÚLTIMA HORA: La bajada desde la Cruz de Ferro hasta Molinaseca acumula casi 1.000 metros de desnivel negativo por sendas muy escarpadas y piedras sueltas; en páginas y foros especializados se la califica de auténtica 'destroza-rodillas'. Bajar despacio y con bastones."
                ),
                PredefinedStageInfo(
                    dayNumber = 26,
                    title = "Etapa 26: Ponferrada a Villafranca del Bierzo",
                    origin = "Ponferrada",
                    destination = "Villafranca del Bierzo",
                    distanceKm = 24.2,
                    elevationGainM = 210,
                    elevationLossM = 190,
                    estimatedTime = "5h 45m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Travesía llana y amable por la hoya berciana entre campos frutales y viñedos de uva mencía. Paso por Camponaraya y Cacabelos, cruzando el río Cúa junto al santuario de la Quinta Angustia. Llegada a Villafranca del Bierzo, la 'pequeña Compostela', con su iglesia románica de Santiago (Puerta del Perdón), colegiata de Santa María y castillo de los marqueses.\nCONSEJOS DE ÚLTIMA HORA: Terreno fácil de transición antes de la gran subida a Galicia. Buen firme alternando pistas de tierra y asfalto secundario. Conviene hidratarse bien y descansar las piernas en Villafranca para acometer la exigente etapa siguiente."
                ),
                PredefinedStageInfo(
                    dayNumber = 27,
                    title = "Etapa 27: Villafranca del Bierzo a O Cebreiro",
                    origin = "Villafranca del Bierzo",
                    destination = "O Cebreiro",
                    distanceKm = 27.8,
                    elevationGainM = 980,
                    elevationLossM = 190,
                    estimatedTime = "7h 30m",
                    difficulty = "Muy exigente",
                    notes = "RECOMENDACIONES: La reina de las etapas de montaña y entrada a Galicia. Se remonta el valle del río Valcarce pasando por Pereje, Trabadelo, La Portela y Vega de Valcarce. En Las Herrerías comienza el durísimo ascenso hacia O Cebreiro pasando por La Faba. Llegada a la mágica aldea de O Cebreiro (1.300 m) con sus pallozas tradicionales, la iglesia de Santa María la Real (relicario del Santo Grial) y la tumba de Don Elías Valiña.\nCONSEJOS DE ÚLTIMA HORA: Desnivel positivo brutal de casi 1.000 metros concentrados en los últimos 8 kilómetros desde Las Herrerías. Subida pedregosa y empinada; dosificar el esfuerzo, no acelerarse y abrigarse arriba porque el viento y la niebla suelen reinar en O Cebreiro."
                ),
                PredefinedStageInfo(
                    dayNumber = 28,
                    title = "Etapa 28: O Cebreiro a Triacastela",
                    origin = "O Cebreiro",
                    destination = "Triacastela",
                    distanceKm = 20.8,
                    elevationGainM = 250,
                    elevationLossM = 840,
                    estimatedTime = "5h 00m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Caminata por las cumbres de la sierra de Ancares y O Courel. Paso por el Alto de San Roque con el monumental monumento de bronce al peregrino desafiando al viento. Descenso hacia Hospital da Condesa y nuevo repecho hacia el Alto do Poio (1.335 m), punto más alto del camino en Galicia. Descenso panorámico hacia Fonfría y llegada a Triacastela, villa jacobea que toma su nombre de los tres castillos que la defendían.\nCONSEJOS DE ÚLTIMA HORA: Tramo de montaña con vistas espectaculares pero muy expuesto a niebla espesa y rachas de viento. El descenso desde el Alto do Poio hasta Triacastela presenta pendientes empinadas; cuidar los apoyos para no resbalar."
                ),
                PredefinedStageInfo(
                    dayNumber = 29,
                    title = "Etapa 29: Triacastela a Sarria",
                    origin = "Triacastela",
                    destination = "Sarria",
                    distanceKm = 18.4,
                    elevationGainM = 280,
                    elevationLossM = 410,
                    estimatedTime = "4h 30m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: En Triacastela se abre una de las bifurcaciones más famosas del Camino: por San Xil (ruta directa más corta por bosques y corredoiras espectaculares) o por Samos (5 km más larga, para admirar el monumental Monasterio benedictino de San Julián de Samos junto al río Sarria). Ambas confluyen en Sarria. Visitar en Sarria la torre del castillo de los marqueses y el convento de la Magdalena.\nCONSEJOS DE ÚLTIMA HORA: La variante de San Xil es puramente rural, con senderos umbríos de robles y castaños centenarios; la variante de Samos discurre más pegada a la carretera pero la visita guiada al monasterio es inolvidable. Ambas opciones tienen su encanto; consultar el clima antes de decidir."
                ),
                PredefinedStageInfo(
                    dayNumber = 30,
                    title = "Etapa 30: Sarria a Portomarín",
                    origin = "Sarria",
                    destination = "Portomarín",
                    distanceKm = 22.2,
                    elevationGainM = 350,
                    elevationLossM = 420,
                    estimatedTime = "5h 30m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Inicio tradicional de los últimos 100 km gallegos. Salida desde Sarria pasando por el convento de la Magdalena y la rúa Maior. Es obligatorio sellar la credencial dos veces al día en Galicia; puedes hacerlo en Barbadelo, en la iglesia románica de Santiago (siglo XII), y en los pequeños bares de Morgade y Ferreiros. Descenso final sobre el embalse de Belesar hacia Portomarín, donde destaca la iglesia-fortaleza de San Nicolás. Salir temprano (7:30 h) para caminar con tranquilidad.\nCONSEJOS DE ÚLTIMA HORA: Los peregrinos en foros alertan sobre la gran afluencia de caminantes desde Sarria. En épocas de lluvia, las corredoiras tradicionales de tierra suelen acumular barro en As Rozas y A Brea. Calzado de membrana o buen agarre muy aconsejable."
                ),
                PredefinedStageInfo(
                    dayNumber = 31,
                    title = "Etapa 31: Portomarín a Palas de Rei",
                    origin = "Portomarín",
                    destination = "Palas de Rei",
                    distanceKm = 24.8,
                    elevationGainM = 450,
                    elevationLossM = 320,
                    estimatedTime = "6h 00m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Salida cruzando el río Miño y subida prolongada hacia la Sierra de Ligonde. Desvío muy recomendable de apenas 500 metros en Castromaior para visitar su impresionante castro prerromano con panorámica completa del valle. En Ligonde detenerse ante el cruceiro de Lameiros del siglo XVII y el antiguo hospital de peregrinos. Parada clásica para comer o sellar en Gonzar o Ventas de Narón.\nCONSEJOS DE ÚLTIMA HORA: Subida continua y tendida de unos 8 kilómetros en el primer tercio. En días soleados este tramo de monte bajo carece de sombras prolongadas; llevar protección solar y cantimplora cargada desde Portomarín."
                ),
                PredefinedStageInfo(
                    dayNumber = 32,
                    title = "Etapa 32: Palas de Rei a Arzúa",
                    origin = "Palas de Rei",
                    destination = "Arzúa",
                    distanceKm = 28.5,
                    elevationGainM = 410,
                    elevationLossM = 480,
                    estimatedTime = "6h 45m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Cruce de la frontera provincial de Lugo a A Coruña a través del puente medieval y la calzada histórica de Leboreiro. Parada obligatoria en Melide para reponer fuerzas con una ración de pulpo á feira en sus pulperías tradicionales y sellar en la iglesia de San Pedro. Unión histórica con el Camino Primitivo. En Arzúa, no dejes de probar el queso local con Denominación de Origen Arzúa-Ulloa.\nCONSEJOS DE ÚLTIMA HORA: Perfil calificado en páginas y foros especializados como rompepiernas debido a los continuos toboganes entre valles fluviales (arroyo de Boente y río Catasol). Controlar el ritmo en las bajadas empedradas y usar bastones para amortiguar el impacto articular."
                ),
                PredefinedStageInfo(
                    dayNumber = 33,
                    title = "Etapa 33: Arzúa a Santiago de Compostela",
                    origin = "Arzúa",
                    destination = "Santiago de Compostela",
                    distanceKm = 38.7,
                    elevationGainM = 590,
                    elevationLossM = 670,
                    estimatedTime = "8h 45m",
                    difficulty = "Exigente",
                    notes = "RECOMENDACIONES: Etapa reina final para quienes unen las dos últimas jornadas en una sola tirada maratoniana (o pernoctan en O Pedrouzo). Paso por Santa Irene con su ermita y fuente barroca, Lavacolla y ascenso al Monte do Gozo para divisar las agujas de la Catedral de Santiago. Entrada triunfal por Porta do Camiño, rúa das Ánimas y plaza de Cervantes hacia la Plaza del Obradoiro bajo el arco del Pazo de Raxoi al son de las gaitas. Sellar en la Oficina del Peregrino para obtener la Compostela.\nCONSEJOS DE ÚLTIMA HORA: Son casi 39 km si se realiza directa; si las fuerzas o el clima no acompañan, en foros se recomienda encarecidamente dividirla haciendo noche en O Pedrouzo o Amenal. Salir antes de las 6:00 h si se hace completa."
                )
            )
        ),

        // 3. Camino Primitivo
        PredefinedRouteInfo(
            title = "Camino Primitivo",
            subtitle = "La primera ruta de peregrinación por la montaña astur-galaica",
            totalKm = 309.2,
            note = null,
            stages = listOf(
                PredefinedStageInfo(
                    dayNumber = 1,
                    title = "Etapa 1: Oviedo a Grado",
                    origin = "Oviedo",
                    destination = "Grado",
                    distanceKm = 25.6,
                    elevationGainM = 420,
                    elevationLossM = 610,
                    estimatedTime = "6h 30m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Salida desde la catedral de San Salvador de Oviedo, origen del célebre dicho jacobeo: 'Quien va a Santiago y no al Salvador, honra al criado y deja al señor'. Sellar en la Cámara Santa de la catedral. Paso por San Lázaro de Paniceres, cruce del río Nora por el puente románico de Gallegos y ascenso a Escamplero. En Grado, recorrer su casco histórico y probar los famosos tocinillos de cielo.\nCONSEJOS DE ÚLTIMA HORA: Etapa con continuos desniveles y sube-y-bajas propios de la orografía asturiana. En días de lluvia el tramo boscoso antes de Premoño y el descenso hacia el río Nalón acumulan barro denso. Salir a primera hora para caminar con calma."
                ),
                PredefinedStageInfo(
                    dayNumber = 2,
                    title = "Etapa 2: Grado a Salas",
                    origin = "Grado",
                    destination = "Salas",
                    distanceKm = 22.2,
                    elevationGainM = 580,
                    elevationLossM = 410,
                    estimatedTime = "5h 45m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Salida cruzando el río Cubia. Ascenso pronunciado hacia el Alto del Fresno con visita obligada al Santuario de la Virgen del Fresno, con espectaculares panorámicas de la cordillera. Descenso al valle de San Marcelo y Cornellana, donde se encuentra el monasterio benedictino de San Salvador (siglo XI) a orillas del río Narcea. Llegada a la villa medieval de Salas, cuna de Fernando Valdés Salas; visitar la colegiata gótica de Santa María la Mayor y la torre medieval.\nCONSEJOS DE ÚLTIMA HORA: La bajada del Alto del Fresno hacia Cornellana por caminos de tierra suele estar resbaladiza por la humedad matinal. Llevar bastones para asegurar los pasos en los descensos empinados."
                ),
                PredefinedStageInfo(
                    dayNumber = 3,
                    title = "Etapa 3: Salas a Tineo",
                    origin = "Salas",
                    destination = "Tineo",
                    distanceKm = 20.2,
                    elevationGainM = 690,
                    elevationLossM = 180,
                    estimatedTime = "5h 30m",
                    difficulty = "Exigente",
                    notes = "RECOMENDACIONES: Subida exigente remontando el valle del río Nonaya. Desvío señalizado de 100 metros para contemplar la hermosa cascada del Nonaya. Paso por La Espina y Bodenaya (célebre albergue de acogida tradicional). Entrada a Tineo, villa de gran tradición jacobea; visitar la iglesia de San Pedro y el monumento al peregrino.\nCONSEJOS DE ÚLTIMA HORA: Ascenso constante de más de 650 metros de desnivel desde Salas hasta La Espina. En foros se destaca la hospitalidad de Bodenaya para sellar y reponer fuerzas antes de los últimos kilómetros a Tineo."
                ),
                PredefinedStageInfo(
                    dayNumber = 4,
                    title = "Etapa 4: Tineo a Pola de Allande",
                    origin = "Tineo",
                    destination = "Pola de Allande",
                    distanceKm = 28.2,
                    elevationGainM = 580,
                    elevationLossM = 720,
                    estimatedTime = "7h 00m",
                    difficulty = "Exigente",
                    notes = "RECOMENDACIONES: Travesía de alta montaña asturiana. Desvío histórico muy recomendado hacia el Monasterio cisterciense de Santa María la Real de Obona (siglo XIII), vinculado al rey Silo y clave en la historia jacobea. Paso por Campiello (punto clave donde los peregrinos deciden si tomar al día siguiente la variante de Hospitales o por Pola). Llegada a Pola de Allande, con el imponente Palacio de Cienfuegos en lo alto.\nCONSEJOS DE ÚLTIMA HORA: Etapa quebrada con senderos boscosos. En Campiello informarse exhaustivamente del parte meteorológico del día siguiente si se planea hacer la variante de Hospitales."
                ),
                PredefinedStageInfo(
                    dayNumber = 5,
                    title = "Etapa 5: Pola de Allande a La Mesa",
                    origin = "Pola de Allande",
                    destination = "La Mesa",
                    distanceKm = 23.3,
                    elevationGainM = 980,
                    elevationLossM = 510,
                    estimatedTime = "6h 30m",
                    difficulty = "Muy exigente",
                    notes = "RECOMENDACIONES: La etapa reina y más sobrecogedora del Camino Primitivo. Ascenso épico al mítico Puerto del Palo (1.146 m) por el camino de pizarra. Vistas inmensas sobre la sierra y los valles verdes. Descenso por Montefurado y Berducedo (visitar su tejo milenario junto a la iglesia) hasta la pequeña aldea de La Mesa.\nCONSEJOS DE ÚLTIMA HORA: Jornada de alta exigencia física con casi 1.000 metros de desnivel. Si hay tormenta, viento fuerte o niebla cerrada, evitar la cresta expuesta. Llevar abundante agua y ropa térmica cortavientos, pues el clima cambia con rapidez en la cumbre."
                ),
                PredefinedStageInfo(
                    dayNumber = 6,
                    title = "Etapa 6: La Mesa a Grandas de Salime",
                    origin = "La Mesa",
                    destination = "Grandas de Salime",
                    distanceKm = 15.5,
                    elevationGainM = 290,
                    elevationLossM = 780,
                    estimatedTime = "4h 30m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Descenso sobrecogedor de más de 800 metros de desnivel hacia el embalse de Salime con impresionantes vistas de la presa y las aguas del río Navia. Parada en el mirador de la presa y cruce del gigantesco muro de hormigón. Ascenso suave hacia Grandas de Salime para visitar su magnífico Museo Etnográfico (uno de los mejores de España) y la colegiata de San Salvador.\nCONSEJOS DE ÚLTIMA HORA: El descenso por pista forestal hacia la presa es muy prolongado y machaca las articulaciones; descender despacio y con bastones. La subida posterior desde la presa a Grandas se hace por asfalto; salir temprano para evitar el calor."
                ),
                PredefinedStageInfo(
                    dayNumber = 7,
                    title = "Etapa 7: Grandas de Salime a A Fonsagrada",
                    origin = "Grandas de Salime",
                    destination = "A Fonsagrada",
                    distanceKm = 25.2,
                    elevationGainM = 850,
                    elevationLossM = 310,
                    estimatedTime = "7h 00m",
                    difficulty = "Muy exigente",
                    notes = "RECOMENDACIONES: Se abandona Asturias y se entra en Galicia coronando el Alto do Acebo (1.030 m). Pueblos de montaña como Castro y Peñafuente. En A Fonsagrada, villa de gran arraigo jacobeo, visitar la Fonte Sagrada que dio nombre al pueblo y probar el célebre pulpo fonsagradino con patatas en sus mesones tradicionales.\nCONSEJOS DE ÚLTIMA HORA: Subida continuada hacia el Alto do Acebo. Arriba suele soplar viento fuerte del norte. En el mojón de entrada a Galicia las flechas y los mojones gallegos indican las distancias restantes a Santiago."
                ),
                PredefinedStageInfo(
                    dayNumber = 8,
                    title = "Etapa 8: A Fonsagrada a O Cádavo",
                    origin = "A Fonsagrada",
                    destination = "O Cádavo",
                    distanceKm = 23.4,
                    elevationGainM = 560,
                    elevationLossM = 710,
                    estimatedTime = "6h 30m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Paso por la aldea de Padrón y ascenso hacia las ruinas del Real Hospital de Santiago de Montouto, fundado por Pedro I el Cruel en el siglo XIV para socorrer a los peregrinos de las ventiscas de nieve. Vistas de molinos de viento en las cumbres de la sierra. Descenso por Paradavella y llegada a O Cádavo (Baleira).\nCONSEJOS DE ÚLTIMA HORA: Terreno montañoso con continuos sube-y-bajas. En Montouto no hay servicios; llevar agua desde A Fonsagrada hasta Paradavella, donde hay bar y tienda para comer."
                ),
                PredefinedStageInfo(
                    dayNumber = 9,
                    title = "Etapa 9: O Cádavo a Lugo",
                    origin = "O Cádavo",
                    destination = "Lugo",
                    distanceKm = 30.3,
                    elevationGainM = 340,
                    elevationLossM = 680,
                    estimatedTime = "7h 15m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Subida matinal al Alto de Vaqueriza y descenso hacia Castroverde, donde destacan la torre del homenaje gótica y la iglesia de Santa María de Vilabade. Entrada monumental en Lugo atravesando la Puerta de San Pedro en su Muralla Romana del siglo III (Patrimonio de la Humanidad, la única en el mundo que se conserva íntegra). Visitar la Catedral de Santa María y tapear por la rúa da Cruz.\nCONSEJOS DE ÚLTIMA HORA: Etapa larga de más de 30 km. El perfil es favorable a partir de Castroverde con pistas de tierra y caminos locales. Reservar fuerzas para disfrutar del paseo sobre la muralla romana en Lugo."
                ),
                PredefinedStageInfo(
                    dayNumber = 10,
                    title = "Etapa 10: Lugo a San Romao da Retorta",
                    origin = "Lugo",
                    destination = "San Romao da Retorta",
                    distanceKm = 19.6,
                    elevationGainM = 260,
                    elevationLossM = 180,
                    estimatedTime = "5h 00m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Salida por la Porta Miñá de la muralla y cruce del puente romano sobre el río Miño. Trayecto apacible por la campiña lucense siguiendo tramos de la antigua calzada romana Vía XIX. Pequeñas aldeas ganaderas como San Vicente do Burgo y Bacurín (iglesia románica de San Miguel). Sellar en la iglesia de San Romao da Retorta, junto al miliario romano.\nCONSEJOS DE ÚLTIMA HORA: Jornada plana y tranquila sin desniveles pronunciados. Pocos servicios en ruta; conviene aprovisionarse en Lugo de agua y aperitivos para la jornada."
                ),
                PredefinedStageInfo(
                    dayNumber = 11,
                    title = "Etapa 11: San Romao a Melide",
                    origin = "San Romao",
                    destination = "Melide",
                    distanceKm = 28.1,
                    elevationGainM = 380,
                    elevationLossM = 460,
                    estimatedTime = "7h 00m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Se abandona la soledad del Primitivo para unirse al bullicio del Camino Francés en Melide. Caminata por corredoiras rurales y prados de Ferreira y As Seixas. En Melide, visitar la capilla de San Roque con su cruceiro del siglo XIV (uno de los más antiguos de Galicia) y la iglesia románica de Santa María. Degustar el pulpo tradicional en las pulperías locales.\nCONSEJOS DE ÚLTIMA HORA: Conexión con el gran flujo de peregrinos del Camino Francés. En los foros se aconseja mentalizarse del cambio de ambiente: de la soledad intimista del Primitivo al ambiente multitudinario de Melide."
                ),
                PredefinedStageInfo(
                    dayNumber = 12,
                    title = "Etapa 12: Melide a O Pedrouzo",
                    origin = "Melide",
                    destination = "O Pedrouzo",
                    distanceKm = 33.3,
                    elevationGainM = 450,
                    elevationLossM = 510,
                    estimatedTime = "8h 00m",
                    difficulty = "Exigente",
                    notes = "RECOMENDACIONES: Caminata compartida con el Camino Francés atravesando Boente, Castañeda y la populosa Arzúa (parada para probar el queso local). Bosques frondosos de robles y eucaliptos hacia Calzada, Salceda y Santa Irene con su ermita. Llegada a O Pedrouzo para la última noche previa a Santiago.\nCONSEJOS DE ÚLTIMA HORA: Cruces continuos de la N-547; respetar los pasos subterráneos y pasos de peatones. Etapa de 33 km si se hace directa desde Melide; dosificar el esfuerzo y hacer paradas regulares."
                ),
                PredefinedStageInfo(
                    dayNumber = 13,
                    title = "Etapa 13: O Pedrouzo a Santiago de Compostela",
                    origin = "O Pedrouzo",
                    destination = "Santiago de Compostela",
                    distanceKm = 19.2,
                    elevationGainM = 310,
                    elevationLossM = 350,
                    estimatedTime = "4h 45m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: La meta soñada. Salida al amanecer por bosques de Amenal hacia Lavacolla. Subida al Monte do Gozo y entrada emocionante a Santiago por la Porta do Camiño, rúa das Ánimas y plaza de Cervantes hasta la Plaza del Obradoiro ante la Catedral de Santiago. Recogida de la Compostela en la Oficina de Acogida al Peregrino.\nCONSEJOS DE ÚLTIMA HORA: Salir muy temprano para llegar a la Misa del Peregrino y saborear la emoción de los últimos kilómetros sin prisas."
                )
            )
        ),

        // 4. Camino del Norte
        PredefinedRouteInfo(
            title = "Camino del Norte",
            subtitle = "De Ribadeo a Santiago por el interior de Galicia",
            totalKm = 195.9,
            note = null,
            stages = listOf(
                PredefinedStageInfo(
                    dayNumber = 1,
                    title = "Etapa 1: Ribadeo a Lourenzá",
                    origin = "Ribadeo",
                    destination = "Lourenzá",
                    distanceKm = 28.4,
                    elevationGainM = 620,
                    elevationLossM = 480,
                    estimatedTime = "7h 00m",
                    difficulty = "Exigente",
                    notes = "RECOMENDACIONES: Entrada a Galicia cruzando la ría del Eo. Salida de Ribadeo pasando por la capilla de San Roque y ascenso suave hacia Vilela y San Vicente de Cubelas. En Lourenzá, visitar el monumental Monasterio de San Salvador fundado por el Conde Santo, con su fachada barroca precursora de la del Obradoiro de Santiago. Probar las afamadas fabas de Lourenzá con I.G.P.\nCONSEJOS DE ÚLTIMA HORA: Etapa dura con repechos continuos en el primer tramo gallego. Páginas y foros especializados destacan que el tramo por montes de eucaliptos puede tener barro tras lluvias; llevar calzado con buen taco."
                ),
                PredefinedStageInfo(
                    dayNumber = 2,
                    title = "Etapa 2: Lourenzá a Abadín",
                    origin = "Lourenzá",
                    destination = "Abadín",
                    distanceKm = 24.8,
                    elevationGainM = 680,
                    elevationLossM = 210,
                    estimatedTime = "6h 15m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Parada en la histórica ciudad episcopal de Mondoñedo: visitar la Catedral de la Asunción (siglo XIII, 'arrodillada'), el santuario de los Remedios y probar la tarta de Mondoñedo en la confitería tradicional del rey de las tartas. Ascenso fuerte hacia la meseta lucense por Lousada hasta Abadín, puerta de la comarca de Terra Chá.\nCONSEJOS DE ÚLTIMA HORA: La subida desde Mondoñedo a Abadín supera los 500 metros de desnivel en pocos kilómetros por pistas de monte; afrontarla con calma y bastones. En la cumbre suele bajar la temperatura bruscamente."
                ),
                PredefinedStageInfo(
                    dayNumber = 3,
                    title = "Etapa 3: Abadín a Vilalba",
                    origin = "Abadín",
                    destination = "Vilalba",
                    distanceKm = 20.6,
                    elevationGainM = 180,
                    elevationLossM = 210,
                    estimatedTime = "5h 00m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Caminata llana y relajante por la planicie de Terra Chá, tierra de prados verdes y explotaciones ganaderas. Paso por Castromaior, Goiriz y su iglesia de Santiago con cementerio gótico. En Vilalba, admirar la imponente Torre del Homenaje de los Andrade (actual Parador) y probar el afamado queso de San Simón da Costa (ahumado con madera de abedul).\nCONSEJOS DE ÚLTIMA HORA: Etapa muy descansada para las piernas tras los desniveles anteriores. Se camina por pistas rurales asfaltadas y caminos de zahorra con escaso tráfico."
                ),
                PredefinedStageInfo(
                    dayNumber = 4,
                    title = "Etapa 4: Vilalba a Baamonde",
                    origin = "Vilalba",
                    destination = "Baamonde",
                    distanceKm = 20.7,
                    elevationGainM = 160,
                    elevationLossM = 180,
                    estimatedTime = "5h 00m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Cruzar el puente medieval de Saa sobre el río Labrada y caminar entre bosques y aldeas ganaderas como Pigara y San Alberto. En Baamonde visitar la iglesia de Santiago (siglo XII), el castaño milenario ahuecado con la Virgen tallada por el escultor Víctor Corral y su casa-museo.\nCONSEJOS DE ÚLTIMA HORA: Jornada cómoda y sombreada. En Baamonde hay estación de tren y buenos albergues; lugar estratégico para descansar antes de la larga etapa a Sobrado."
                ),
                PredefinedStageInfo(
                    dayNumber = 5,
                    title = "Etapa 5: Baamonde a Sobrado dos Monxes",
                    origin = "Baamonde",
                    destination = "Sobrado dos Monxes",
                    distanceKm = 40.3,
                    elevationGainM = 620,
                    elevationLossM = 540,
                    estimatedTime = "9h 30m",
                    difficulty = "Muy exigente",
                    notes = "RECOMENDACIONES: La etapa maratón del Camino del Norte (muchos peregrinos la dividen haciendo noche en Miraz). Paso por la aldea de Miraz y ascenso a la sierra de Corno de Boi, entrando en la provincia de A Coruña. Llegada a Sobrado dos Monxes, con su impresionante Monasterio cisterciense de Santa María (siglo XII), donde es posible alojarse en la hospedería monástica y asistir al canto de vísperas.\nCONSEJOS DE ÚLTIMA HORA: Con más de 40 km, en los foros se recomienda encarecidamente evaluar las propias fuerzas: si no se quiere hacer completa, pernoctar en el albergue de Miraz (km 15). Cargar agua y comida ya que no hay servicios entre Miraz y Marcela."
                ),
                PredefinedStageInfo(
                    dayNumber = 6,
                    title = "Etapa 6: Sobrado dos Monxes a Arzúa",
                    origin = "Sobrado dos Monxes",
                    destination = "Arzúa",
                    distanceKm = 21.4,
                    elevationGainM = 260,
                    elevationLossM = 340,
                    estimatedTime = "5h 15m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Salida rodeando la laguna de Sobrado (construida por los monjes en el siglo XVI). Paseo entre robledales y aldeas de Boimorto y Sendelle (iglesia de Santa María con pinturas murales del siglo XV). Llegada a Arzúa y unión histórica con el Camino Francés.\nCONSEJOS DE ÚLTIMA HORA: Etapa agradable y de perfil descendente. En Arzúa se nota el cambio brusco de ambiente al coincidir con los peregrinos que vienen del Camino Francés y Primitivo."
                ),
                PredefinedStageInfo(
                    dayNumber = 7,
                    title = "Etapa 7: Arzúa a O Pedrouzo",
                    origin = "Arzúa",
                    destination = "O Pedrouzo",
                    distanceKm = 19.3,
                    elevationGainM = 280,
                    elevationLossM = 320,
                    estimatedTime = "4h 45m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Senda compartida con el Camino Francés por hermosas carballeiras gallegas y aldeas rurales como Salceda, Brea y Santa Irene (ermita y fuente barroca). En O Pedrouzo descansar y preparar la llegada final a Santiago.\nCONSEJOS DE ÚLTIMA HORA: Atención a los cruces de la carretera N-547; utilizar siempre los pasos seguros. Madrugar al día siguiente para el tramo final."
                ),
                PredefinedStageInfo(
                    dayNumber = 8,
                    title = "Etapa 8: O Pedrouzo a Santiago de Compostela",
                    origin = "O Pedrouzo",
                    destination = "Santiago de Compostela",
                    distanceKm = 19.4,
                    elevationGainM = 310,
                    elevationLossM = 350,
                    estimatedTime = "4h 45m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Salida al alba atravesando bosques de eucaliptos por Lavacolla hasta el Monte do Gozo. Primer vistazo a las torres de la Catedral. Entrada triunfal a Santiago de Compostela por Porta do Camiño y llegada emocionante a la Plaza del Obradoiro.\nCONSEJOS DE ÚLTIMA HORA: Dirigirse a la Oficina de Acogida al Peregrino para obtener la Compostela y disfrutar de la Misa del Peregrino."
                )
            )
        ),

        // 5. Camino Portugués Central
        PredefinedRouteInfo(
            title = "Camino Portugués Central",
            subtitle = "De Tui a Santiago entre viñedos de albariño y rías gallegas",
            totalKm = 115.5,
            note = null,
            stages = listOf(
                PredefinedStageInfo(
                    dayNumber = 1,
                    title = "Etapa 1: Tui a O Porriño",
                    origin = "Tui",
                    destination = "O Porriño",
                    distanceKm = 16.5,
                    elevationGainM = 190,
                    elevationLossM = 180,
                    estimatedTime = "4h 00m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Cruzar el Puente Internacional sobre el río Miño y visitar la impresionante Catedral fortificada de Santa María de Tui. Sellar en la catedral y en el convento de las Clarisas (degustar sus tradicionales pececitos de almendra). Salir sobre las 8:00 h para evitar las horas de calor en la veiga del río Louro.\nCONSEJOS DE ÚLTIMA HORA: Tomar sin dudar la variante fluvial señalizada del río Louro (senda botánica) para evitar por completo el monótono y peligroso polígono industrial de As Gándaras, tal como se recalca unánimemente en páginas y foros especializados."
                ),
                PredefinedStageInfo(
                    dayNumber = 2,
                    title = "Etapa 2: O Porriño a Redondela",
                    origin = "O Porriño",
                    destination = "Redondela",
                    distanceKm = 15.2,
                    elevationGainM = 240,
                    elevationLossM = 260,
                    estimatedTime = "3h 45m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Paso por el municipio de Mos, con su Pazo de los Marqueses de Mos y la iglesia de Santa Eulalia. Sellar en el albergue de peregrinos de Santa Bárbara. Al coronar el Alto de Enxertade, disfrutar de las primeras vistas a la ría de Vigo y la ensenada de San Simón con sus famosos viaductos ferroviarios decimonónicos.\nCONSEJOS DE ÚLTIMA HORA: Subida corta pero intensa hacia Os Valos y Chan das Pipas por asfalto con tráfico local moderado. El descenso hacia Redondela es pronunciado; moderar el paso para proteger las rodillas."
                ),
                PredefinedStageInfo(
                    dayNumber = 3,
                    title = "Etapa 3: Redondela a Pontevedra",
                    origin = "Redondela",
                    destination = "Pontevedra",
                    distanceKm = 19.8,
                    elevationGainM = 360,
                    elevationLossM = 350,
                    estimatedTime = "5h 15m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Paso por Arcade, célebre por sus ostras, y cruce del histórico Ponte Sampaio sobre el río Verdugo, escenario de la batalla de 1809 contra las tropas napoleónicas. En Pontevedra, visitar la iglesia de la Virgen Peregrina (con singular planta de vieira), la basílica de Santa María la Mayor y sus animadas plazas del casco histórico como la plaza de la Leña y de la Verdura.\nCONSEJOS DE ÚLTIMA HORA: Subida pedregosa por la Brea Vella da Canicouva que puede estar resbaladiza con lluvia. A la entrada de Pontevedra, tomar la variante fluvial del río Gafos señalizada a la izquierda para evitar el polígono y la carretera general."
                ),
                PredefinedStageInfo(
                    dayNumber = 4,
                    title = "Etapa 4: Pontevedra a Caldas de Reis",
                    origin = "Pontevedra",
                    destination = "Caldas de Reis",
                    distanceKm = 21.1,
                    elevationGainM = 220,
                    elevationLossM = 240,
                    estimatedTime = "5h 15m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Salida cruzando el río Lérez por el puente del Burgo. Caminata apacible entre viñedos de albariño emparrados, hórreos y aldeas del valle del Salnés. En Caldas de Reis, relajarse sumergiendo los pies en las aguas termales públicas de la fuente de As Burgas (que manan a unos 50 grados) y cruzar el puente romano sobre el río Umia. Iglesia de Santo Tomás Becket.\nCONSEJOS DE ÚLTIMA HORA: En foros se recuerda que a la altura de Barro se encuentra el Parque Natural del Río Barosa, accesible desde este tramo o en el arranque del siguiente. Etapa mayoritariamente llana con tramos de tierra y pistas asfaltadas tranquilas."
                ),
                PredefinedStageInfo(
                    dayNumber = 5,
                    title = "Etapa 5: Caldas de Reis a Padrón",
                    origin = "Caldas de Reis",
                    destination = "Padrón",
                    distanceKm = 18.6,
                    elevationGainM = 210,
                    elevationLossM = 220,
                    estimatedTime = "4h 30m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Parada imprescindible e ineludible en el Parque Natural del Río Barosa (A Barosa), situado a escasa distancia del camino, para contemplar sus espectaculares cascadas escalonadas y el conjunto etnográfico de catorce molinos tradicionales de agua. Visitar la iglesia de Santa María de Iria Flavia, antigua sede episcopal, y en Padrón la iglesia de Santiago donde se conserva bajo el altar mayor el Pedrón (ara romana donde según la tradición se amarró la barca apostólica de piedra). Degustar los auténticos pimientos de Padrón y visitar la casa museo de Rosalía de Castro.\nCONSEJOS DE ÚLTIMA HORA: Fuentes de peregrinos y foros especializados insisten en que el desvío a la cascada de Barosa merece totalmente la pena; hay zona de sombra y cafetería para reponer fuerzas. La subida a Santa Mariña de Carracedo es suave pero constante entre sombreados bosques de robles."
                ),
                PredefinedStageInfo(
                    dayNumber = 6,
                    title = "Etapa 6: Padrón a Santiago de Compostela",
                    origin = "Padrón",
                    destination = "Santiago de Compostela",
                    distanceKm = 24.3,
                    elevationGainM = 410,
                    elevationLossM = 160,
                    estimatedTime = "6h 00m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Paso por el santuario de A Escravitude y su fuente milagrosa, donde es tradicional sellar la credencial. En O Milladoiro se tiene la primera vista lejana de las torres de la Catedral. Entrada a Santiago por Porta Faxeira hacia la rúa do Franco y llegada triunfal a la Plaza del Obradoiro ante la fachada de la Catedral de Santiago. Sellar y recoger la Compostela en la rúa Carretas.\nCONSEJOS DE ÚLTIMA HORA: Etapa con desnivel moderado y varios tramos de asfalto y núcleos urbanos continuos. Salir temprano para evitar horas de tráfico en la aproximación a Santiago y disponer de tiempo suficiente en la Oficina del Peregrino."
                )
            )
        ),

        // 6. Camino Inglés
        PredefinedRouteInfo(
            title = "Camino Inglés",
            subtitle = "De Ferrol a Santiago de Compostela",
            totalKm = 120.8,
            note = "Se recomienda realizar el Camino por el trazado antiguo",
            stages = listOf(
                PredefinedStageInfo(
                    dayNumber = 1,
                    title = "Etapa 1: Ferrol a Neda",
                    origin = "Ferrol",
                    destination = "Neda",
                    distanceKm = 15.0,
                    elevationGainM = 160,
                    elevationLossM = 150,
                    estimatedTime = "3h 45m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Esta etapa se realiza por el Trazado Antiguo tradicional. La ruta parte del muelle de Curuxeiras (km 0 del Camino Inglés) en Ferrol, donde atracaban los barcos de peregrinos del norte de Europa. Sellar en el punto de información del muelle y en la concatedral de San Julián. El Trazado Antiguo bordea la ría de Ferrol por caminos costeros y senderos rurales tradicionales, evitando tramos de carretera provincial y polígonos industriales de las modificaciones posteriores. En Neda no te pierdas la iglesia de Santa María y su famoso pan de Neda elaborado con agua del río Belelle. Conviene salir sobre las 8:00 h para caminar con calma bordeando la ría.\nCONSEJOS DE ÚLTIMA HORA: Peregrinos en páginas y foros especializados destacan que el Trazado Antiguo por el borde de la ría es mucho más seguro y paisajístico que el arcén asfaltado. Precaución con la marea alta en pequeños pasos ribereños y llevar calzado cómodo ya que predomina el firme duro en el primer tercio."
                ),
                PredefinedStageInfo(
                    dayNumber = 2,
                    title = "Etapa 2: Neda a Pontedeume",
                    origin = "Neda",
                    destination = "Pontedeume",
                    distanceKm = 16.0,
                    elevationGainM = 280,
                    elevationLossM = 290,
                    estimatedTime = "4h 00m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Esta etapa se realiza por el Trazado Antiguo tradicional. La ruta sube por pistas vecinales y correderas tradicionales evitando el tráfico pesado de la N-651 en el entorno de Fene y Cabanas. Cruzar el puente de piedra sobre el río Eume y subir al casco histórico medieval de Pontedeume. Visitar la iglesia de Santiago y contemplar el torreón de los Andrade. Sellar en el albergue de Neda o en la iglesia de San Martiño de Porto en Cabanas.\nCONSEJOS DE ÚLTIMA HORA: En páginas y foros especializados se comenta que el Trazado Antiguo tiene repechos pronunciados en Fene y O Vilar pero evita los peligrosos cruces de la carretera nacional. Al descender hacia la playa de Cabanas el pavimento puede resbalar con lluvia o humedad matinal."
                ),
                PredefinedStageInfo(
                    dayNumber = 3,
                    title = "Etapa 3: Pontedeume a Betanzos",
                    origin = "Pontedeume",
                    destination = "Betanzos",
                    distanceKm = 20.2,
                    elevationGainM = 450,
                    elevationLossM = 430,
                    estimatedTime = "5h 15m",
                    difficulty = "Moderada",
                    notes = "RECOMENDACIONES: Esta etapa se realiza por el Trazado Antiguo tradicional. Este trazado histórico discurre por senderos de monte y pistas de tierra de Boebre y Miño, evitando las largas variantes de asfalto y enlaces de autopista introducidos en reformas recientes. Parada en la ría de Miño y cruce del puente medieval sobre el río Lambre. En Betanzos de los Caballeros recorrer sus tres puertas medievales, la iglesia gótica de Santa María del Azogue y probar la famosa tortilla de Betanzos. Sellar en la iglesia de San Francisco.\nCONSEJOS DE ÚLTIMA HORA: Etapa rompepiernas con constantes subidas y bajadas cortas pero duras. En páginas y foros especializados los senderistas alertan del repecho empinado de salida de Pontedeume hacia O Monte y de la bajada pedregosa hacia Miño. Bastones muy recomendados."
                ),
                PredefinedStageInfo(
                    dayNumber = 4,
                    title = "Etapa 4: Betanzos a Hospital de Bruma",
                    origin = "Betanzos",
                    destination = "Hospital de Bruma",
                    distanceKm = 28.3,
                    elevationGainM = 670,
                    elevationLossM = 210,
                    estimatedTime = "7h 00m",
                    difficulty = "Exigente",
                    notes = "RECOMENDACIONES: Esta etapa se realiza por el Trazado Antiguo tradicional. El trazado antiguo asciende por las históricas corredoiras de monte y bosques autóctonos de Vilacova y Presedo, evitando las pistas de concentración parcelaria y tramos de asfalto abierto al tráfico. Esta es la etapa reina por su desnivel acumulado y soledad. Sellar en el albergue de Presedo o en la iglesia de San Esteban de Cos. En Hospital de Bruma visitar el emplazamiento del antiguo hospital de peregrinos medieval fundado en 1140.\nCONSEJOS DE ÚLTIMA HORA: En los foros se insiste en salir muy temprano y llevar suficiente agua y comida ya que no hay apenas servicios abiertos entre Presedo y As Travesas. El Trazado Antiguo ofrece mayor sombra en el duro ascenso pero puede presentar barro en zonas sombrías."
                ),
                PredefinedStageInfo(
                    dayNumber = 5,
                    title = "Etapa 5: Hospital de Bruma a Sigüeiro",
                    origin = "Hospital de Bruma",
                    destination = "Sigüeiro",
                    distanceKm = 24.8,
                    elevationGainM = 190,
                    elevationLossM = 380,
                    estimatedTime = "6h 00m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Esta etapa se realiza por el Trazado Antiguo tradicional. Discurre por pistas forestales de tierra y caminos tradicionales entre robles y eucaliptos en lugar de las rectas interminables de carretera local. Terreno prácticamente llano y de perfil descendente que cruza el municipio de Ordes. Visitar la iglesia de San Julián de Poulo y sellar en su bar cercano. En Sigüeiro disfrutar del parque fluvial junto al río Tambre y su puente medieval.\nCONSEJOS DE ÚLTIMA HORA: Páginas y foros especializados advierten de la escasez de fuentes operativas en los primeros 12 kilómetros; llenar cantimplora en Bruma. El Trazado Antiguo se agradece por la amortiguación del suelo de tierra frente a las rodillas castigadas."
                ),
                PredefinedStageInfo(
                    dayNumber = 6,
                    title = "Etapa 6: Sigüeiro a Santiago de Compostela",
                    origin = "Sigüeiro",
                    destination = "Santiago de Compostela",
                    distanceKm = 16.5,
                    elevationGainM = 260,
                    elevationLossM = 210,
                    estimatedTime = "4h 15m",
                    difficulty = "Fácil",
                    notes = "RECOMENDACIONES: Esta etapa se realiza por el Trazado Antiguo tradicional. Sigue el trazado histórico que entra a Santiago atravesando el bosque da Sionlla y el valle del río Tambre, minimizando el paso por el polígono industrial del Tambre a través de corredores verdes históricos. Salida temprana para llegar a Santiago antes del mediodía. Cruzar el puente medieval sobre el río Tambre. Entrada por la Porta do Camiño hacia la Plaza de Cervantes, rúa da Acibechería y la majestuosa Plaza del Obradoiro. Sellar la credencial en la Oficina de Acogida al Peregrino para obtener la Compostela.\nCONSEJOS DE ÚLTIMA HORA: Los peregrinos en foros destacan que la aproximación por el Trazado Antiguo hace la entrada mucho más agradable y natural. Atención al tráfico al aproximarse a San Caetano y disfrutar con calma de los últimos kilómetros."
                )
            )
        ),

        // 7. Vía de la Plata
        PredefinedRouteInfo(
            title = "Vía de la Plata",
            subtitle = "La gran calzada romana de Sevilla a Astorga",
            totalKm = 705.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Sevilla a Guillena", "Sevilla", "Guillena", 22.8, 80, 60, "5h 15m", "Fácil",
                    "RECOMENDACIONES: Salida desde la majestuosa Catedral de Sevilla por el puente de Triana. Paso por Camas y Santiponce, donde es visita obligada el conjunto arqueológico de Itálica con su anfiteatro romano y mosaicos. Sellar en el monasterio de San Isidoro del Campo. Llegada a Guillena cruzando el río Rivera de Huelva.\nCONSEJOS DE ÚLTIMA HORA: Etapa llana pero muy expuesta al sol andaluz. Imprescindible salir con las primeras luces del alba y llevar abundante agua; las sombras son prácticamente inexistentes en el tramo agrícola final."),
                PredefinedStageInfo(2, "Etapa 2: Guillena a Castilblanco de los Arroyos", "Guillena", "Castilblanco de los Arroyos", 18.2, 340, 40, "4h 30m", "Fácil",
                    "RECOMENDACIONES: Comienzo suave de las estribaciones de Sierra Morena. Caminata entre dehesas de encinas, alcornocales y toros bravos. En Castilblanco de los Arroyos visitar la iglesia del Divino Salvador y degustar la gastronomía serrana (chacinas y miel artesana).\nCONSEJOS DE ÚLTIMA HORA: Subida tendida pero constante en la segunda mitad. Cargar agua en Guillena, pues no hay puntos intermedios habilitados hasta llegar a Castilblanco."),
                PredefinedStageInfo(3, "Etapa 3: Castilblanco a Almadén de la Plata", "Castilblanco de los Arroyos", "Almadén de la Plata", 28.8, 480, 390, "7h 00m", "Exigente",
                    "RECOMENDACIONES: Jornada paisajística extraordinaria atravesando el Parque Natural de la Sierra Norte de Sevilla. Tramo de casi 16 km por carretera asfaltada tranquila hasta la entrada de la finca pública Las Navas del Berrocal. Ascenso al Cerro del Calvario con panorámica sobrecogedora y descenso a Almadén de la Plata (visitar la torre del reloj y la iglesia de Santa María de Gracia).\nCONSEJOS DE ÚLTIMA HORA: La bajada del Calvario a Almadén es muy empinada y con lajas resbaladizas; usar bastones. Llevar provisiones completas, no hay tiendas ni bares en casi 30 km."),
                PredefinedStageInfo(4, "Etapa 4: Almadén de la Plata a Monesterio", "Almadén de la Plata", "Monesterio", 34.4, 520, 310, "8h 15m", "Exigente",
                    "RECOMENDACIONES: Se abandona Andalucía y se entra en Extremadura. Paso por El Real de la Jara y su castillo medieval del siglo XIV. Subida al puerto de la Cruz y entrada en Badajoz hacia Monesterio, cuna del jamón ibérico de bellota. Visitar el Museo del Jamón.\nCONSEJOS DE ÚLTIMA HORA: Etapa larga y exigente físicamente. En foros se recomienda dividirla durmiendo en El Real de la Jara si se quiere caminar sin prisas."),
                PredefinedStageInfo(5, "Etapa 5: Monesterio a Fuente de Cantos", "Monesterio", "Fuente de Cantos", 21.2, 90, 320, "5h 00m", "Fácil",
                    "RECOMENDACIONES: Descenso suave hacia los llanos de la comarca de Tentudía entre dehesas y campos de cereal. Llegada a Fuente de Cantos, cuna del pintor Francisco de Zurbarán (visitar su casa-museo) y la iglesia barroca de Nuestra Señora de la Granada.\nCONSEJOS DE ÚLTIMA HORA: Terreno plano y monótono por pistas de tierra en buen estado. Protegerse del sol y del viento de cara, habitual en esta meseta."),
                PredefinedStageInfo(6, "Etapa 6: Fuente de Cantos a Zafra", "Fuente de Cantos", "Zafra", 24.6, 140, 180, "5h 45m", "Fácil",
                    "RECOMENDACIONES: Paso por Calzadilla de los Barros con su iglesia de El Salvador y su retablo mayor gótico. Llegada a Zafra, la 'Sevilla Chica': Plaza Grande y Plaza Chica comunicadas por el Arquillo del Pan, y el imponente Alcázar de los Duques de Feria (actual Parador).\nCONSEJOS DE ÚLTIMA HORA: Pistas agrícolas de tierra compactada. Salir temprano para llegar a mediodía a Zafra y recorrer su casco histórico con tranquilidad."),
                PredefinedStageInfo(7, "Etapa 7: Zafra a Villafranca de los Barros", "Zafra", "Villafranca de los Barros", 19.2, 110, 170, "4h 30m", "Fácil",
                    "RECOMENDACIONES: Paso por Los Santos de Maimona con su iglesia de Nuestra Señora de los Ángeles. Entrada en la comarca de Tierra de Barros entre interminables extensiones de viñedos y olivos. En Villafranca de los Barros, 'Ciudad de la Música', visitar la iglesia de Nuestra Señora del Valle.\nCONSEJOS DE ÚLTIMA HORA: Terreno llano y cómodo sin dificultades técnicas. Caminos anchos de tierra entre viñedos con escasa vegetación de sombra."),
                PredefinedStageInfo(8, "Etapa 8: Villafranca de los Barros a Almendralejo", "Villafranca de los Barros", "Almendralejo", 13.0, 70, 90, "3h 15m", "Fácil",
                    "RECOMENDACIONES: Jornada corta y relajada por el corazón vitivinícola de Badajoz. Almendralejo, capital de Tierra de Barros y cuna del Romanticismo (Espronceda y Carolina Coronado). Visitar el Museo de las Ciencias del Vino y catar sus afamados caldos y cavas.\nCONSEJOS DE ÚLTIMA HORA: Ideal para recuperar fuerzas de etapas anteriores. Firme de pistas agrícolas con buen drenaje."),
                PredefinedStageInfo(9, "Etapa 9: Almendralejo a Torremejía", "Almendralejo", "Torremejía", 13.1, 50, 70, "3h 15m", "Fácil",
                    "RECOMENDACIONES: Paisaje llano de suaves ondulaciones hacia la vega del Guadiana. Torremejía, escenario literario de 'La familia de Pascual Duarte' de Camilo José Cela. Palacio de los Lastres y ermita de la Piedad.\nCONSEJOS DE ÚLTIMA HORA: Muchos peregrinos unen esta etapa con la anterior o continúan hasta Mérida (29 km en total). Cuidado en los cruces con la carretera N-630."),
                PredefinedStageInfo(10, "Etapa 10: Torremejía a Mérida", "Torremejía", "Mérida", 15.7, 90, 160, "4h 00m", "Fácil",
                    "RECOMENDACIONES: Entrada espectacular a la Augusta Emerita romana cruzando el soberbio Puente Romano sobre el río Guadiana (de casi 800 metros de longitud). Visita imprescindible al Teatro y Anfiteatro romanos, Templo de Diana, Alcazaba árabe y Museo Nacional de Arte Romano.\nCONSEJOS DE ÚLTIMA HORA: Salir temprano para dedicar toda la tarde a explorar los impresionantes monumentos romanos de Mérida. Sellar en el albergue o en la basílica de Santa Eulalia."),
                PredefinedStageInfo(11, "Etapa 11: Mérida a Alcuéscar", "Mérida", "Alcuéscar", 36.4, 380, 180, "8h 30m", "Exigente",
                    "RECOMENDACIONES: Salida pasando junto al Acueducto de los Milagros y el milenario Embalse romano de Proserpina. Dehesas extremeñas hacia El Carrascalejo y Aljucén (parque natural de Cornalvo). Llegada a Alcuéscar y visita imprescindible a la basílica hispanovisigoda de Santa Lucía del Trampal (siglo VII).\nCONSEJOS DE ÚLTIMA HORA: Etapa maratón de 36 km sin apenas servicios entre Aljucén y Alcuéscar (casi 20 km de dehesa). Cargar agua y comida abundante; foros recomiendan dividir en Aljucén si se desea."),
                PredefinedStageInfo(12, "Etapa 12: Alcuéscar a Cáceres", "Alcuéscar", "Cáceres", 38.2, 280, 290, "8h 45m", "Exigente",
                    "RECOMENDACIONES: Caminata solitaria siguiendo la antigua calzada romana jalonada por miliarios originales. Paso por Casas de Don Antonio (puente romano) y Valdesalor. Entrada triunfal a la ciudad monumental de Cáceres (Patrimonio de la Humanidad): Plaza Mayor, Torre de Bujaco, concatedral de Santa María y palacios medievales.\nCONSEJOS DE ÚLTIMA HORA: Otra etapa de gran distancia (38 km). Es vital reponer agua en Valdesalor antes del repecho del puerto de las Camellas hacia Cáceres."),
                PredefinedStageInfo(13, "Etapa 13: Cáceres a Cañaveral", "Cáceres", "Cañaveral", 33.4, 240, 310, "7h 45m", "Exigente",
                    "RECOMENDACIONES: Travesía por los llanos de Cáceres hacia Casar de Cáceres, famoso por la Torta del Casar con D.O.P. Cruce del imponente río Tajo sobre el puente del embalse de Alcántara. Ascenso al puerto de los Castaños y llegada a Cañaveral.\nCONSEJOS DE ÚLTIMA HORA: Tramo desértico entre Casar de Cáceres y el embalse de Alcántara sin puntos de agua. Llevar provisiones suficientes y calzado amortiguado."),
                PredefinedStageInfo(14, "Etapa 14: Cañaveral a Galisteo", "Cañaveral", "Galisteo", 28.2, 210, 320, "6h 45m", "Moderada",
                    "RECOMENDACIONES: Ascenso al Puerto de los Castaños entre alcornocales y descenso hacia el valle del Alagón. Visita a Grimaldo y llegada a Galisteo, famosa por su intacta muralla almohade de cantos rodados del siglo XII y la torre de la Picota.\nCONSEJOS DE ÚLTIMA HORA: Subida inicial pronunciada por senda pedregosa en el puerto de los Castaños. En foros se aconseja caminar por el adarve de la muralla en Galisteo al atardecer."),
                PredefinedStageInfo(15, "Etapa 15: Galisteo a Cáparra", "Galisteo", "Cáparra", 29.7, 230, 190, "7h 00m", "Moderada",
                    "RECOMENDACIONES: Paso por Riolobos y Carcaboso. Se camina sobre la auténtica calzada romana Vía de la Plata bordeada por encinas y miliarios. Llegada al yacimiento arqueológico de Cáparra con su célebre Arco Tetrapilo cuadrifronte, único en la península ibérica.\nCONSEJOS DE ÚLTIMA HORA: Si el albergue de Cáparra no está operativo, en foros se recomienda planificar alojamiento en Aldeanueva del Camino u Oliva de Plasencia."),
                PredefinedStageInfo(16, "Etapa 16: Cáparra a Baños de Montemayor", "Cáparra", "Baños de Montemayor", 28.5, 410, 140, "6h 45m", "Moderada",
                    "RECOMENDACIONES: Paso por Aldeanueva del Camino con sus puentes sobre el río Garganta de San Andrés. Ascenso hacia el valle del Ambroz hasta Baños de Montemayor, villa termal romana; disfrutar de sus termas y sellar en su ayuntamiento o balneario.\nCONSEJOS DE ÚLTIMA HORA: Ascenso constante en los últimos 10 km. Caminos con tramos de calzada romana empedrada; bastones útiles para tracción."),
                PredefinedStageInfo(17, "Etapa 17: Baños de Montemayor a Calzada de Béjar", "Baños de Montemayor", "Calzada de Béjar", 15.5, 360, 210, "4h 00m", "Fácil",
                    "RECOMENDACIONES: Se supera el Puerto de Béjar (frontera entre Extremadura y Castilla y León) disfrutando de vistas a la Sierra de Béjar. Entrada en Salamanca hacia Calzada de Béjar, pequeño pueblo con arquitectura serrana de piedra.\nCONSEJOS DE ÚLTIMA HORA: Etapa corta y de montaña suave. El firme asciende por sendas boscosas de robles y castaños muy agradables."),
                PredefinedStageInfo(18, "Etapa 18: Calzada de Béjar a Fuenterroble", "Calzada de Béjar", "Fuenterroble de Salvatierra", 20.2, 210, 180, "5h 00m", "Fácil",
                    "RECOMENDACIONES: Paso por Valverde de Valdelacasa y Valdelacasa. En Fuenterroble de Salvatierra, parada legendaria en el albergue parroquial del Padre Blas, impulsor histórico de la Vía de la Plata, y parque temático de la calzada romana.\nCONSEJOS DE ÚLTIMA HORA: Terreno cómodo y ondulado entre dehesas charras de encinas y ganado vacuno morucho."),
                PredefinedStageInfo(19, "Etapa 19: Fuenterroble a San Pedro de Rozados", "Fuenterroble de Salvatierra", "San Pedro de Rozados", 28.6, 290, 250, "6h 45m", "Moderada",
                    "RECOMENDACIONES: Ascenso al Pico Dueña (1.170 m), punto más alto de la Vía de la Plata coronado por la Cruz de Santiago y un parque eólico con panorámica infinita. Descenso hacia San Pedro de Rozados pasando por dehesas de toros de lidia.\nCONSEJOS DE ÚLTIMA HORA: Tramo expuesto al viento en el Pico Dueña. No hay servicios entre Fuenterroble y San Pedro de Rozados; cargar comida y agua para toda la jornada."),
                PredefinedStageInfo(20, "Etapa 20: San Pedro de Rozados a Salamanca", "San Pedro de Rozados", "Salamanca", 23.0, 90, 280, "5h 30m", "Fácil",
                    "RECOMENDACIONES: Descenso suave atravesando Morille y Miranda de Azán. Entrada triunfal a Salamanca por el Puente Romano sobre el río Tormes. Visitar la Plaza Mayor, la Catedral Vieja y Nueva, la fachada plateresca de la Universidad y la Casa de las Conchas.\nCONSEJOS DE ÚLTIMA HORA: Etapa en bajada con vistas a las catedrales salmantinas desde el cerro de los Montalvos. Entrar temprano para disfrutar de la ciudad universitaria."),
                PredefinedStageInfo(21, "Etapa 21: Salamanca a El Cubo de la Tierra del Vino", "Salamanca", "El Cubo de la Tierra del Vino", 36.4, 210, 160, "8h 30m", "Exigente",
                    "RECOMENDACIONES: Salida por Aldeaseca de la Armuña hacia Calzada de Valdunciel (fuente y miliario romano). Cruce del límite con Zamora hacia El Cubo de la Tierra del Vino, tierra de cepas antiguas y bodegas tradicionales subterráneas.\nCONSEJOS DE ÚLTIMA HORA: Etapa muy larga (36 km) por pistas de concentración parcelaria sin sombras. En foros se sugiere pernoctar en Calzada de Valdunciel si se desea recortar."),
                PredefinedStageInfo(22, "Etapa 22: El Cubo de la Tierra del Vino a Zamora", "El Cubo de la Tierra del Vino", "Zamora", 31.8, 140, 260, "7h 30m", "Exigente",
                    "RECOMENDACIONES: Paso por Villanueva de Campeán y su convento franciscano en ruinas, y San Marcial. Entrada en Zamora cruzando el río Duero por el puente de Piedra. Descubrir la 'Perla del Románico': Catedral con cúpula bizantina escamada y una veintena de iglesias románicas.\nCONSEJOS DE ÚLTIMA HORA: Caminos de grava y pistas llanas. Salir muy temprano para llegar a Zamora con tiempo de visitar su excepcional patrimonio románico."),
                PredefinedStageInfo(23, "Etapa 23: Zamora a Riego del Camino", "Zamora", "Riego del Camino", 33.8, 180, 140, "7h 45m", "Exigente",
                    "RECOMENDACIONES: Salida hacia Roales del Pan y Montamarta, bordeando la cola del embalse del río Esla con la ermita de la Virgen del Castillo. Continuación por la meseta cerealista hasta Riego del Camino o Fontanillas de Castro.\nCONSEJOS DE ÚLTIMA HORA: Si el embalse de Ricobayo está en su cota máxima, en foros advierten que el paso de Montamarta puede inundarse; seguir la variante señalizada por carretera."),
                PredefinedStageInfo(24, "Etapa 24: Riego del Camino a Granja de Moreruela", "Riego del Camino", "Granja de Moreruela", 8.0, 40, 50, "2h 00m", "Fácil",
                    "RECOMENDACIONES: Jornada corta de descanso y reflexión. Llegada a Granja de Moreruela, encrucijada histórica donde la Vía de la Plata se bifurca hacia Astorga o hacia el Camino Sanabrés. Desvío de 3 km imprescindible al Monasterio cisterciense de Santa María de Moreruela (siglo XII).\nCONSEJOS DE ÚLTIMA HORA: Etapa ideal para relajarse y decidir si se continúa hacia Astorga o se toma el Camino Sanabrés por Ourense."),
                PredefinedStageInfo(25, "Etapa 25: Granja de Moreruela a Benavente", "Granja de Moreruela", "Benavente", 25.5, 120, 160, "6h 00m", "Fácil",
                    "RECOMENDACIONES: Para quienes siguen la traza de la Vía de la Plata hacia Astorga. Paso por Santovenia del Esla y Villaveza del Agua. Entrada en Benavente, visitando la Torre del Caracol (Parador) y las iglesias románicas de Santa María del Azogue y San Juan del Mercado.\nCONSEJOS DE ÚLTIMA HORA: Caminata paralela al río Esla por terreno llano. Buen firme y servicios regulares en los pueblos intermedios."),
                PredefinedStageInfo(26, "Etapa 26: Benavente a Astorga", "Benavente", "Astorga", 46.0, 310, 180, "10h 30m", "Muy exigente",
                    "RECOMENDACIONES: Unión final de la Vía de la Plata con el Camino Francés. Paso por Alija del Infantado (castillo de los Pimentel), La Bañeza (famosa por sus alubias con I.G.P.) y cruce del río Tuerto hacia Astorga. Entrada ante la Catedral y el Palacio de Gaudí.\nCONSEJOS DE ÚLTIMA HORA: Por su enorme distancia (46 km), esta etapa se divide casi universalmente haciendo noche en La Bañeza (km 24). Consultar albergues en foros de peregrinos.")
            )
        ),

        // 8. Camino Sanabrés
        PredefinedRouteInfo(
            title = "Camino Sanabrés",
            subtitle = "De Granja de Moreruela a Santiago por las sierras de Zamora y Ourense",
            totalKm = 367.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Granja de Moreruela a Tábara", "Granja de Moreruela", "Tábara", 25.3, 190, 140, "6h 00m", "Moderada",
                    "RECOMENDACIONES: Arranque del Camino Sanabrés en Granja de Moreruela hacia las estribaciones de la Sierra de la Culebra. Cruce del río Esla por el puente Quintos. En Tábara visitar la iglesia de Santa María y el Centro de Interpretación de los Beatos mozárabes.\nCONSEJOS DE ÚLTIMA HORA: Tramo solitario de monte bajo. Tras cruzar el puente Quintos hay un sendero arenoso que asciende suavemente. Cargar agua en Granja."),
                PredefinedStageInfo(2, "Etapa 2: Tábara a Santa Croya de Tera", "Tábara", "Santa Croya de Tera", 22.5, 160, 180, "5h 15m", "Fácil",
                    "RECOMENDACIONES: Paso por Bercianos de Valverde y Santa María de Valverde. Entrada en el fértil valle del río Tera por Santa Croya y Santa Marta de Tera, con su famosa escultura románica de Santiago Peregrino sonriente del siglo XI en la portada sur.\nCONSEJOS DE ÚLTIMA HORA: Caminos llanos y agradables entre huertas y choperas. Visita indispensable a la iglesia románica de Santa Marta de Tera para sellar."),
                PredefinedStageInfo(3, "Etapa 3: Santa Croya de Tera a Rionegro del Puente", "Santa Croya de Tera", "Rionegro del Puente", 27.5, 240, 120, "6h 30m", "Moderada",
                    "RECOMENDACIONES: Caminata siguiendo el curso del río Tera por Calzadilla, Olleros de Tera y Villar de Farfón. En Rionegro del Puente, visitar el célebre Santuario de la Virgen de la Carballeda y la Cofradía de los Falifos, una de las asociaciones asistenciales jacobeas más antiguas del mundo.\nCONSEJOS DE ÚLTIMA HORA: Sendas sombreadas por encinas y robles. En Villar de Farfón suele haber hospitalidad tradicional en el albergue."),
                PredefinedStageInfo(4, "Etapa 4: Rionegro del Puente a Puebla de Sanabria", "Rionegro del Puente", "Puebla de Sanabria", 40.5, 410, 280, "9h 30m", "Muy exigente",
                    "RECOMENDACIONES: Gran jornada sanabresa pasando por Mombuey (espectacular torre románica de los Templarios) y Asturianos. Llegada a Puebla de Sanabria, conjunto histórico encaramado sobre el río Tera: Castillo de los Condes de Benavente e iglesia de Nuestra Señora del Azogue.\nCONSEJOS DE ÚLTIMA HORA: Etapa muy larga (40 km); muchos peregrinos la dividen haciendo noche en Mombuey o Asturianos. En páginas y foros especializados se elogia el encanto medieval de Puebla de Sanabria."),
                PredefinedStageInfo(5, "Etapa 5: Puebla de Sanabria a Lubián", "Puebla de Sanabria", "Lubián", 28.5, 620, 480, "7h 00m", "Exigente",
                    "RECOMENDACIONES: Entrada en la Alta Sanabria. Paso por Requejo y ascenso al Puerto del Padornelo (1.360 m). Paisaje de montaña con robles, brezo y arroyos cristalinos. Descenso a Lubián, pueblo de arquitectura tradicional de piedra y pizarra con su santuario de la Tuiza.\nCONSEJOS DE ÚLTIMA HORA: Subida montañosa exigente al Padornelo; en invierno y primavera puede haber nieve o heladas matinales. Llevar ropa de abrigo adecuada."),
                PredefinedStageInfo(6, "Etapa 6: Lubián a A Gudiña", "Lubián", "A Gudiña", 24.0, 580, 420, "6h 00m", "Moderada",
                    "RECOMENDACIONES: Se abandona Castilla y León y se entra en Galicia coronando el mítico Puerto de A Canda (1.260 m). Mojón gallego de bienvenida en Vilavella (visitar sus aguas sulfurosas). Llegada a A Gudiña, nudo histórico de comunicaciones montañesas.\nCONSEJOS DE ÚLTIMA HORA: En A Gudiña se presenta la célebre bifurcación: por Laza (ruta más salvaje y montañosa) o por Verín. La mayoría de los peregrinos eligen Laza por su belleza paisajística."),
                PredefinedStageInfo(7, "Etapa 7: A Gudiña a Laza", "A Gudiña", "Laza", 34.5, 460, 890, "8h 15m", "Exigente",
                    "RECOMENDACIONES: Una de las etapas más espectaculares y solitarias de toda España. Vistas vertiginosas al embalse de As Portas y descenso épico por Campobecerros hacia el valle de Laza, cuna del ancestral Entroido gallego (los Peliqueiros).\nCONSEJOS DE ÚLTIMA HORA: Casi 35 km de alta montaña sin apenas núcleos habitados. Cargar agua y comida abundante. El descenso hacia Laza por senda pedregosa requiere buen control de bastones."),
                PredefinedStageInfo(8, "Etapa 8: Laza a Xunqueira de Ambía", "Laza", "Xunqueira de Ambía", 32.2, 590, 480, "7h 45m", "Exigente",
                    "RECOMENDACIONES: Ascenso matinal al monte de Alberguería, donde en el mítico bar Rincón del Peregrino cuelgan miles de vieiras dedicadas por caminantes de todo el mundo. Descenso a Vilar de Barrio y travesía por la llanura de A Limia hasta Xunqueira de Ambía con su colegiata románica de Santa María.\nCONSEJOS DE ÚLTIMA HORA: La subida a Alberguería es empinada entre castaños centenarios; tomarla con calma. Parada obligatoria para sellar en Alberguería y dejar tu firma."),
                PredefinedStageInfo(9, "Etapa 9: Xunqueira de Ambía a Ourense", "Xunqueira de Ambía", "Ourense", 22.0, 140, 490, "5h 15m", "Fácil",
                    "RECOMENDACIONES: Descenso hacia el valle del río Miño pasando por pequeñas aldeas agrícolas. Entrada triunfal en Ourense: cruzar el Puente Romano, visitar la Catedral de San Martín con su Pórtico del Paraíso y bañarse en las famosas termas de As Burgas (aguas calientes al aire libre).\nCONSEJOS DE ÚLTIMA HORA: La entrada a Ourense incluye varios kilómetros de pavimento urbano por el polígono de San Cibrao; tomárselo con calma."),
                PredefinedStageInfo(10, "Etapa 10: Ourense a Cea", "Ourense", "Cea", 22.2, 580, 210, "5h 45m", "Moderada",
                    "RECOMENDACIONES: Salida cruzando el Miño y subida pronunciada por Cudeiro y Tamallancos. Llegada a San Cristovo de Cea, capital del célebre Pan de Cea con I.G.P. horneado en hornos tradicionales de leña. Posibilidad de desvío al colosal Monasterio de Santa María la Real de Oseira (el 'Escorial gallego').\nCONSEJOS DE ÚLTIMA HORA: Subida inicial de 5 km muy empinada saliendo de Ourense. En foros se recomienda vivamente la variante que pasa por el Monasterio de Oseira para pernoctar en su hospedería."),
                PredefinedStageInfo(11, "Etapa 11: Cea a A Laxe", "Cea", "A Laxe", 37.3, 560, 610, "8h 45m", "Exigente",
                    "RECOMENDACIONES: Cruce de las tierras de Deza y Carballeda pasando por Dozón y Lalín. En A Laxe (Bendoiro) se encuentra un modélico albergue público de la Xunta y se une el Camino de Invierno con el Sanabrés.\nCONSEJOS DE ÚLTIMA HORA: Etapa larga que muchos peregrinos acortan saliendo desde Oseira o pernoctando en Dozón. Senda rural entre corredoiras y prados verdes."),
                PredefinedStageInfo(12, "Etapa 12: A Laxe a Outeiro", "A Laxe", "Outeiro", 33.0, 410, 490, "7h 45m", "Exigente",
                    "RECOMENDACIONES: Paso por Silleda y Bandeira (famosa por sus empanadas tradicionales). Cruce del río Ulla por el espectacular puente medieval de Ponte Ulla, frontera natural entre Pontevedra y A Coruña. Ascenso al pico Sacro y llegada a Outeiro (Vedra).\nCONSEJOS DE ÚLTIMA HORA: Descenso muy empinado hacia Ponte Ulla y posterior subida exigente hacia Outeiro. Paisaje gallego auténtico de robledales y viñedos."),
                PredefinedStageInfo(13, "Etapa 13: Outeiro a Santiago de Compostela", "Outeiro", "Santiago de Compostela", 16.7, 240, 310, "4h 15m", "Fácil",
                    "RECOMENDACIONES: Últimos compases del Camino Sanabrés. Vista panorámica del Pico Sacro (vinculado a la traslación del cuerpo de Santiago). Entrada a la ciudad por Angrois, Calzada de Sar y Colegiata de Santa María de Sar. Entrada al casco histórico por Porta de Mazarelos y llegada triunfal a la Plaza del Obradoiro.\nCONSEJOS DE ÚLTIMA HORA: Etapa corta y emocionante para llegar temprano a la Catedral y celebrar el final de la gran travesía sanabresa.")
            )
        ),

        // 9. Camino Vasco del Interior
        PredefinedRouteInfo(
            title = "Camino Vasco del Interior",
            subtitle = "De Irún a Santo Domingo de la Calzada por el túnel de San Adrián",
            totalKm = 203.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Irún a Hernani", "Irún", "Hernani", 27.2, 620, 580, "6h 45m", "Exigente",
                    "RECOMENDACIONES: Salida desde Irún pasando por Oiartzun y Astigarraga (cuna de la sidra vasca o sagardoa). Casco histórico de Hernani, declarado conjunto monumental, con la iglesia de San Juan Bautista.\nCONSEJOS DE ÚLTIMA HORA: Etapa quebrada con senderos boscosos y tramos de carril bici (bidegorri). Cuidado con la señalización en las inmediaciones urbanas."),
                PredefinedStageInfo(2, "Etapa 2: Hernani a Tolosa", "Hernani", "Tolosa", 19.4, 380, 340, "4h 45m", "Fácil",
                    "RECOMENDACIONES: Se remonta el valle del río Oria pasando por Andoain y Villabona. Llegada a Tolosa, antigua capital de Guipúzcoa; admirar la iglesia de Santa María, el Tinglado y degustar sus famosas alubias de Tolosa con sacramentos.\nCONSEJOS DE ÚLTIMA HORA: Jornada cómoda siguiendo el fondo del valle junto al río Oria, con abundante sombra y servicios regulares."),
                PredefinedStageInfo(3, "Etapa 3: Tolosa a Zerain", "Tolosa", "Zerain", 23.1, 540, 280, "5h 45m", "Moderada",
                    "RECOMENDACIONES: Paso por Ordizia (famoso mercado tradicional) y Beasain (conjunto monumental de Igartza con su palacio y ferrería). Ascenso a Zerain, precioso pueblo rural guipuzcoano con minas de hierro históricas.\nCONSEJOS DE ÚLTIMA HORA: Subida progresiva hacia la comarca del Goierri. El entorno de Zerain es tranquilo y hospitalario; reservar en su albergue rural."),
                PredefinedStageInfo(4, "Etapa 4: Zerain a Salvatierra / Agurain", "Zerain", "Salvatierra", 28.7, 890, 580, "7h 30m", "Muy exigente",
                    "RECOMENDACIONES: La etapa mítica del Camino Vasco. Ascenso hacia la Sierra de Aizkorri cruzando la calzada medieval y el increíble Túnel de San Adrián (paso natural horadado en la roca viva con una ermita en su interior). Entrada en Álava y llegada a Salvatierra/Agurain con sus iglesias fortificadas de San Juan y Santa María.\nCONSEJOS DE ÚLTIMA HORA: Exigencia de alta montaña. En caso de lluvia o niebla en San Adrián, el suelo de piedra caliza está muy resbaladizo; usar calzado de montaña y bastones."),
                PredefinedStageInfo(5, "Etapa 5: Salvatierra a Vitoria-Gasteiz", "Salvatierra", "Vitoria-Gasteiz", 27.8, 160, 240, "6h 30m", "Fácil",
                    "RECOMENDACIONES: Travesía por la llanura alavesa entre campos de cereal y pequeñas iglesias románicas como Gazeo (pinturas murales góticas) y Alaiza. Entrada a Vitoria-Gasteiz: Catedral de Santa María ('Abierto por obras' que inspiró a Ken Follett) y almendra medieval.\nCONSEJOS DE ÚLTIMA HORA: Terreno llano y cómodo por pistas parcelarias. Etapa larga pero sin desniveles pronunciados."),
                PredefinedStageInfo(6, "Etapa 6: Vitoria-Gasteiz a La Puebla de Arganzón", "Vitoria-Gasteiz", "La Puebla de Arganzón", 18.6, 120, 210, "4h 30m", "Fácil",
                    "RECOMENDACIONES: Salida hacia Armentia para visitar la basílica de San Prudencio (joya del románico alavés). Paso por Subijana y entrada en el Enclave de Treviño hacia La Puebla de Arganzón, villa amurallada junto al río Zadorra con su puente medieval.\nCONSEJOS DE ÚLTIMA HORA: Jornada agradable y corta que permite recuperar fuerzas. Buen firme de caminos vecinales."),
                PredefinedStageInfo(7, "Etapa 7: La Puebla de Arganzón a Haro", "La Puebla de Arganzón", "Haro", 31.0, 310, 360, "7h 15m", "Moderada",
                    "RECOMENDACIONES: Paso por Armiñón y cruce de las Conchas de Haro, desfiladero natural donde el río Ebro entra en La Rioja. Llegada a Haro, capital del Rioja: Barrio de la Estación con las bodegas centenarias más prestigiosas del mundo.\nCONSEJOS DE ÚLTIMA HORA: Etapa de más de 30 km. En páginas y foros especializados se recomienda salir temprano para disfrutar de la tarde en las bodegas históricas de Haro."),
                PredefinedStageInfo(8, "Etapa 8: Haro a Santo Domingo de la Calzada", "Haro", "Santo Domingo de la Calzada", 27.2, 290, 180, "6h 30m", "Moderada",
                    "RECOMENDACIONES: Caminata entre mares de viñedos riojanos pasando por Casalarreina (monasterio de Nuestra Señora de la Piedad) y Bañares (iglesia de la Santa Cruz). Llegada a Santo Domingo de la Calzada y unión triunfal con el Camino Francés.\nCONSEJOS DE ÚLTIMA HORA: Caminos de tierra entre viñas en buen estado. Visitar la Catedral de Santo Domingo para contemplar el milagro del gallo y la gallina.")
            )
        ),

        // 10. Camino del Baztán
        PredefinedRouteInfo(
            title = "Camino del Baztán",
            subtitle = "De Bayona a Pamplona a través del valle atlántico de Baztán",
            totalKm = 109.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Bayona a Ustaritz", "Bayona", "Ustaritz", 14.3, 140, 120, "3h 30m", "Fácil",
                    "RECOMENDACIONES: Salida desde la catedral gótica de Santa María de Bayona. Plácido paseo fluvial siguiendo el curso del río Nive por el camino de sirga (camino de halage). Llegada a Ustaritz, capital histórica de la provincia vascofrancesa de Labort.\nCONSEJOS DE ÚLTIMA HORA: Terreno completamente llano por camino asfaltado y tierra compactada. Ideal para una primera toma de contacto con el camino."),
                PredefinedStageInfo(2, "Etapa 2: Ustaritz a Urdazubi / Urdax", "Ustaritz", "Urdazubi", 21.2, 480, 390, "5h 15m", "Moderada",
                    "RECOMENDACIONES: Paso por Souraïde y la preciosa villa labortana de Ainhoa (fachadas blancas y rojas del siglo XVII). Cruce de la frontera con Navarra en Dantxarinea. En Urdazubi/Urdax visitar el monasterio de San Salvador y sus famosas cuevas prehistóricas de Ikaburua.\nCONSEJOS DE ÚLTIMA HORA: Subida suave pero continuada hacia la frontera. El paisaje transfronterizo de colinas verdes es de gran belleza; sellar en el monasterio de Urdax."),
                PredefinedStageInfo(3, "Etapa 3: Urdazubi / Urdax a Elizondo", "Urdazubi", "Elizondo", 18.8, 560, 480, "5h 00m", "Moderada",
                    "RECOMENDACIONES: Ascenso al mítico Puerto de Otsondo (574 m) con su ermita y merendero entre hayas. Descenso al valle de Baztán pasando por Amaiur/Maya (famoso monolito del castillo y molino harinero). Llegada a Elizondo, capital del valle, con sus palacios señoriales y el puente de Txokoto sobre el río Baztán.\nCONSEJOS DE ÚLTIMA HORA: Subida exigente por senda montañosa a Otsondo; en días de niebla seguir estrictamente las flechas amarillas. En Elizondo probar el chocolate urrakin egiña (con avellanas enteras)."),
                PredefinedStageInfo(4, "Etapa 4: Elizondo a Berroeta", "Elizondo", "Berroeta", 9.7, 280, 90, "2h 30m", "Fácil",
                    "RECOMENDACIONES: Corto y delicioso paseo entre caseríos tradicionales baztaneses, praderas con vacas y hórreos. Paso por Lekaroz, Irurita (palacio barroco de Jauregia) y Zigaurre con el impresionante mirador de Baztán. Llegada a la tranquila aldea de Berroeta.\nCONSEJOS DE ÚLTIMA HORA: Etapa corta concebida para descansar o como tramo previo al paso del puerto de Belate al día siguiente."),
                PredefinedStageInfo(5, "Etapa 5: Berroeta a Olagüe", "Berroeta", "Olagüe", 20.4, 610, 420, "5h 30m", "Exigente",
                    "RECOMENDACIONES: El gran reto del Baztán: el ascenso al histórico Puerto de Belate (847 m) atravesando densos hayedos, la ermita medieval de Santiago de Belate y tramos de calzada romana. Descenso al valle de Anué hacia Olagüe.\nCONSEJOS DE ÚLTIMA HORA: Zona de montaña húmeda propensa a barro y niebla cerrada en Belate. Llevar ropa de abrigo, calzado impermeable y bastones. No hay servicios entre Almandoz y Venta de San Blas."),
                PredefinedStageInfo(6, "Etapa 6: Olagüe a Pamplona", "Olagüe", "Pamplona", 24.6, 210, 390, "5h 45m", "Fácil",
                    "RECOMENDACIONES: Caminata siguiendo el curso del río Ultzama pasando por Ostiz y Sorauren. En Trinidad de Arre cruzar el puente medieval donde confluyen el Camino del Baztán y el Camino Francés. Entrada triunfal a Pamplona por el Portal de Francia.\nCONSEJOS DE ÚLTIMA HORA: Terreno descendente y muy cómodo por pistas de tierra y paseos fluviales. Entrada a Pamplona compartida con los peregrinos del Camino Francés.")
            )
        ),

        // 11. Camino de San Salvador
        PredefinedRouteInfo(
            title = "Camino de San Salvador",
            subtitle = "De León a Oviedo por la cordillera cantábrica y el puerto de Pajares",
            totalKm = 122.0,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: León a La Robla", "León", "La Robla", 26.3, 240, 80, "6h 15m", "Fácil",
                    "RECOMENDACIONES: Salida desde la catedral de León (Pulchra Leonina) bordeando el río Bernesga por Carbajal de la Legua y Cuadros. Paso por Cabanillas y Cascantes. Llegada a la villa minera de La Robla con su ermita de Celada y albergue municipal.\nCONSEJOS DE ÚLTIMA HORA: Etapa de transición llana hacia la cordillera Cantábrica. Pistas de tierra paralelas a la vía y al río Bernesga. Cargar agua en Cuadros."),
                PredefinedStageInfo(2, "Etapa 2: La Robla a Poladura de la Tercia", "La Robla", "Poladura de la Tercia", 23.9, 450, 110, "5h 45m", "Moderada",
                    "RECOMENDACIONES: Ascenso paulatino hacia la alta montaña leonesa pasando por Puente de Alba y La Pola de Gordón (visitar el santuario del Buen Suceso). En Buiza se asciende hacia las Hoces de Buiza y el puerto de San Antón hasta Poladura de la Tercia.\nCONSEJOS DE ÚLTIMA HORA: El paso de las Hoces de Buiza es espectacular pero empinado y pedregoso; si hay mal tiempo o nieve, en foros se aconseja la variante por Villasimpliz y Villamanín."),
                PredefinedStageInfo(3, "Etapa 3: Poladura de la Tercia a Pajares", "Poladura de la Tercia", "Pajares", 15.8, 620, 810, "5h 00m", "Muy exigente",
                    "RECOMENDACIONES: La etapa reina y mítica de alta montaña cantábrica. Ascenso a la Cruz de San Salvador y collado de Canto La Rasa (1.500 m) entre pastos alpinos, caballos y vistas sublimes a las dos vertientes. Cruce del límite con Asturias en el Puerto de Pajares y descenso a la aldea de Pajares.\nCONSEJOS DE ÚLTIMA HORA: Terreno de alta montaña sin cobertura ni refugios intermedios. Es obligatorio consultar el parte meteorológico antes de salir de Poladura. Con niebla cerrada no aventurarse sin GPS o track descargado."),
                PredefinedStageInfo(4, "Etapa 4: Pajares a Pola de Lena", "Pajares", "Pola de Lena", 26.2, 280, 1050, "6h 30m", "Exigente",
                    "RECOMENDACIONES: Descenso pronunciado por el valle del río Pajares entre castañares y hayedos por San Miguel del Río y Llanos de Somerón. En Puente de los Fierros contemplar el puente medieval. Desvío obligado de 500 metros en Campomanes hacia Santa Cristina de Lena (siglo IX), cumbre universal del arte prerrománico asturiano. Llegada a Pola de Lena.\nCONSEJOS DE ÚLTIMA HORA: El descenso desde Pajares castiga intensamente rodillas y cuádriceps con más de 1.000 metros de desnivel negativo. Usar bastones y bajar con calma."),
                PredefinedStageInfo(5, "Etapa 5: Pola de Lena a Oviedo", "Pola de Lena", "Oviedo", 30.4, 420, 590, "7h 15m", "Moderada",
                    "RECOMENDACIONES: Travesía por las cuencas mineras asturianas pasando por Mieres del Camín y Olloniego (conjunto histórico del palacio de Quirós y puente medieval). Ascenso final al Padrún y entrada triunfal a Oviedo hacia la Santa Basílica Catedral de San Salvador, completando la gran promesa: 'Quien va a Santiago y no al Salvador, honra al criado y deja al señor'. Sellar y recoger la Salvadorana.\nCONSEJOS DE ÚLTIMA HORA: Sendero que combina tramos verdes con sendas fluviales y pasos urbanos. Salir temprano para llegar a tiempo a la catedral y solicitar la acreditación de la Salvadorana.")
            )
        ),

        // 12. Epílogo a Fisterra y Muxía
        PredefinedRouteInfo(
            title = "Finisterre y Muxía",
            subtitle = "El epílogo hacia el Fin del Mundo y la Costa da Morte",
            totalKm = 117.2,
            note = null,
            stages = listOf(
                PredefinedStageInfo(1, "Etapa 1: Santiago de Compostela a Negreira", "Santiago de Compostela", "Negreira", 21.0, 420, 560, "5h 30m", "Moderada",
                    "RECOMENDACIONES: Salida desde la Plaza del Obradoiro bajando por rúa das Hortas hacia Carretas y monte de San Pedro. Descenso al valle de Augapesada y cruce mágico de Ponte Maceira sobre el río Tambre, uno de los pueblos más bellos de España con su puente medieval de arcos apuntados, presas de molino y casonas de piedra. Llegada a Negreira, con el Pazo de Cotón y capilla de San Mauro.\nCONSEJOS DE ÚLTIMA HORA: Subida exigente al Alto do Mar de Ovellas tras Augapesada por camino empedrado y húmedo. Salir sobre las 8:00 h para disfrutar de la mañana en Ponte Maceira."),
                PredefinedStageInfo(2, "Etapa 2: Negreira a Olveiroa", "Negreira", "Olveiroa", 33.4, 610, 480, "8h 15m", "Exigente",
                    "RECOMENDACIONES: La etapa más solitaria y profunda de la Galicia rural interior. Caminata entre robledales y hórreos centenarios pasando por Zas, A Pena, Vilaserío y Santa Mariña. En Olveiroa, aldea modelo de rehabilitación etnográfica, admirar sus hórreos de piedra y cantería tradicional gallega.\nCONSEJOS DE ÚLTIMA HORA: Etapa larga (33 km) con escasos servicios entre Vilaserío y Santa Mariña. Cargar agua y comida. Si las fuerzas flaquean, se puede pernoctar en Vilaserío o Santa Mariña."),
                PredefinedStageInfo(3, "Etapa 3: Olveiroa a Cee", "Olveiroa", "Cee", 19.8, 220, 480, "5h 00m", "Fácil",
                    "RECOMENDACIONES: Paso por el embalse de Castrelo y Hospital, donde se encuentra la encrucijada entre Fisterra y Muxía. Hacia Fisterra, se asciende por el monte de San Pedro Mártir (ermita y fuente milagrosa). Momento cumbre al coronar el Alto das Cruces y divisar por primera vez la inmensidad del océano Atlántico y el cabo Fisterra. Descenso hacia Corcubión (villa marinera declarada conjunto histórico) y Cee.\nCONSEJOS DE ÚLTIMA HORA: En Hospital asegúrate de seguir los mojones hacia Fisterra si es tu destino inicial. La primera vista al mar emociona profundamente a todos los peregrinos."),
                PredefinedStageInfo(4, "Etapa 4: Cee a Fisterra", "Cee", "Fisterra", 13.7, 290, 280, "3h 45m", "Fácil",
                    "RECOMENDACIONES: Plácida caminata bordeando la costa por la playa de Estorde y la larguísima playa de Langosteira (dos kilómetros de arena blanca donde es tradición mojarse los pies). Llegada a la villa de Fisterra y subida final de 3 km al legendario Faro de Finisterre (el Fin del Mundo romano, Cabo Fisterra, km 0). Contemplar la puesta de sol sobre el Atlántico y recoger la Fisterrana.\nCONSEJOS DE ÚLTIMA HORA: Etapa corta concebida para disfrutar con calma. En páginas y foros especializados se recuerda no quemar ropa ni calzado en el faro (práctica prohibida y contaminante); disfrutar de la puesta de sol con respeto ambiental."),
                PredefinedStageInfo(5, "Etapa 5: Fisterra a Muxía", "Fisterra", "Muxía", 29.3, 590, 590, "7h 30m", "Exigente",
                    "RECOMENDACIONES: Enlace costero de la Costa da Morte entre Fisterra y Muxía. Paso por aldeas rurales como San Martiño de Duio, Lires (con su hermosa ría y puesta de sol) y Morquintián. Entrada en Muxía hacia la Punta da Barca para visitar el Santuario da Virxe da Barca frente a las olas atlánticas y las legendarias piedras mágicas (Pedra de Abalar y Pedra dos Cadrís). Recoger la Muxiana.\nCONSEJOS DE ÚLTIMA HORA: Etapa quebrada con subidas y bajadas continuas entre bosques y costa. Cargar avituallamiento en Lires. Extremar la precaución con el oleaje en las rocas del Santuario da Barca.")
            )
        )
    )

    fun getLocalizedTitle(title: String, mode: LanguageMode): String {
        if (mode == LanguageMode.ES) return title
        return when (title) {
            "Camino Francés (últimos 100 km)" -> when (mode) {
                LanguageMode.EN -> "French Way (Last 100 km)"
                LanguageMode.FR -> "Chemin Français (100 derniers km)"
                LanguageMode.IT -> "Cammino Francese (Ultimi 100 km)"
                LanguageMode.DE -> "Französischer Weg (Letzte 100 km)"
                LanguageMode.NL -> "Franse Weg (Laatste 100 km)"
                LanguageMode.PL -> "Szlak Francuski (Ostatnie 100 km)"
                LanguageMode.ZH -> "法国之路（最后 100 公里）"
                LanguageMode.KO -> "프랑스 길 (마지막 100km)"
                LanguageMode.PT -> "Caminho Francês (últimos 100 km)"
                else -> title
            }
            "Camino Francés" -> when (mode) {
                LanguageMode.EN -> "French Way (Complete)"
                LanguageMode.FR -> "Chemin Français Complet"
                LanguageMode.IT -> "Cammino Francese Completo"
                LanguageMode.DE -> "Französischer Weg (Komplett)"
                LanguageMode.NL -> "Volledige Franse Weg"
                LanguageMode.PL -> "Szlak Francuski (Kompletny)"
                LanguageMode.ZH -> "完整法国之路"
                LanguageMode.KO -> "프랑스 길 (전체)"
                LanguageMode.PT -> "Caminho Francês Completo"
                else -> title
            }
            "Camino Primitivo" -> when (mode) {
                LanguageMode.EN -> "Primitive Way"
                LanguageMode.FR -> "Chemin Primitif"
                LanguageMode.IT -> "Cammino Primitivo"
                LanguageMode.DE -> "Ursprünglicher Weg"
                LanguageMode.NL -> "Primitieve Weg"
                LanguageMode.PL -> "Szlak Pierwotny"
                LanguageMode.ZH -> "原始之路"
                LanguageMode.KO -> "원시 길"
                LanguageMode.PT -> "Caminho Primitivo"
                else -> title
            }
            "Camino del Norte" -> when (mode) {
                LanguageMode.EN -> "Northern Way"
                LanguageMode.FR -> "Chemin du Nord"
                LanguageMode.IT -> "Cammino del Nord"
                LanguageMode.DE -> "Nordweg"
                LanguageMode.NL -> "Noordelijke Weg"
                LanguageMode.PL -> "Szlak Północny"
                LanguageMode.ZH -> "北方之路"
                LanguageMode.KO -> "북쪽 길"
                LanguageMode.PT -> "Caminho do Norte"
                else -> title
            }
            "Camino Portugués Central" -> when (mode) {
                LanguageMode.EN -> "Portuguese Way (Central)"
                LanguageMode.FR -> "Chemin Portugais (Central)"
                LanguageMode.IT -> "Cammino Portoghese Centrale"
                LanguageMode.DE -> "Portugiesischer Weg (Zentral)"
                LanguageMode.NL -> "Portugese Weg (Centraal)"
                LanguageMode.PL -> "Szlak Portugalski Centralny"
                LanguageMode.ZH -> "葡萄牙中心之路"
                LanguageMode.KO -> "포르투갈 길 (중앙)"
                LanguageMode.PT -> "Caminho Português Central"
                else -> title
            }
            "Camino Inglés" -> when (mode) {
                LanguageMode.EN -> "English Way"
                LanguageMode.FR -> "Chemin Anglais"
                LanguageMode.IT -> "Cammino Inglese"
                LanguageMode.DE -> "Englischer Weg"
                LanguageMode.NL -> "Engelse Weg"
                LanguageMode.PL -> "Szlak Angielski"
                LanguageMode.ZH -> "英国之路"
                LanguageMode.KO -> "영국 길"
                LanguageMode.PT -> "Caminho Inglês"
                else -> title
            }
            "Vía de la Plata" -> when (mode) {
                LanguageMode.EN -> "Silver Way"
                LanguageMode.FR -> "Voie de la Plata"
                LanguageMode.IT -> "Via de la Plata"
                LanguageMode.DE -> "Silberweg"
                LanguageMode.NL -> "Zilverweg"
                LanguageMode.PL -> "Srebrny Szlak"
                LanguageMode.ZH -> "白银之路"
                LanguageMode.KO -> "은의 길"
                LanguageMode.PT -> "Via de la Plata"
                else -> title
            }
            "Camino Sanabrés" -> when (mode) {
                LanguageMode.EN -> "Sanabrés Way"
                LanguageMode.FR -> "Chemin Sanabrais"
                LanguageMode.IT -> "Cammino Sanabrese"
                LanguageMode.DE -> "Sanabrés-Weg"
                LanguageMode.NL -> "Sanabrés Weg"
                LanguageMode.PL -> "Szlak Sanabrés"
                LanguageMode.ZH -> "萨纳夫雷斯之路"
                LanguageMode.KO -> "사나브레스 길"
                LanguageMode.PT -> "Caminho Sanabrês"
                else -> title
            }
            "Camino Vasco del Interior" -> when (mode) {
                LanguageMode.EN -> "Basque Inland Way"
                LanguageMode.FR -> "Chemin Basque de l'Intérieur"
                LanguageMode.IT -> "Cammino Basco dell'Interno"
                LanguageMode.DE -> "Baskischer Inlandsweg"
                LanguageMode.NL -> "Baskische Binnenlandse Weg"
                LanguageMode.PL -> "Baskijski Szlak Wewnętrzny"
                LanguageMode.ZH -> "巴斯克内陆之路"
                LanguageMode.KO -> "바스크 내륙 길"
                LanguageMode.PT -> "Caminho Basco do Interior"
                else -> title
            }
            "Camino del Baztán" -> when (mode) {
                LanguageMode.EN -> "Baztán Way"
                LanguageMode.FR -> "Chemin du Baztán"
                LanguageMode.IT -> "Cammino del Baztán"
                LanguageMode.DE -> "Baztán-Weg"
                LanguageMode.NL -> "Baztán Weg"
                LanguageMode.PL -> "Szlak Baztán"
                LanguageMode.ZH -> "巴兹坦之路"
                LanguageMode.KO -> "바스탄 길"
                LanguageMode.PT -> "Caminho do Baztán"
                else -> title
            }
            "Camino de San Salvador" -> when (mode) {
                LanguageMode.EN -> "San Salvador Way"
                LanguageMode.FR -> "Chemin de Saint-Sauveur"
                LanguageMode.IT -> "Cammino di San Salvador"
                LanguageMode.DE -> "San Salvador-Weg"
                LanguageMode.NL -> "San Salvador Weg"
                LanguageMode.PL -> "Szlak San Salvador"
                LanguageMode.ZH -> "圣萨尔瓦多之路"
                LanguageMode.KO -> "산 살바도르 길"
                LanguageMode.PT -> "Caminho de San Salvador"
                else -> title
            }
            "Finisterre y Muxía" -> when (mode) {
                LanguageMode.EN -> "Finisterre and Muxía"
                LanguageMode.FR -> "Finisterre et Muxía"
                LanguageMode.IT -> "Finisterre e Muxía"
                LanguageMode.DE -> "Finisterre und Muxía"
                LanguageMode.NL -> "Finisterre en Muxía"
                LanguageMode.PL -> "Finisterre i Muxía"
                LanguageMode.ZH -> "菲尼斯特雷与穆希亚"
                LanguageMode.KO -> "피니스테레 및 무시아"
                LanguageMode.PT -> "Finisterra e Muxía"
                else -> title
            }
            else -> title
        }
    }

    fun getLocalizedSubtitle(subtitle: String, mode: LanguageMode): String {
        if (mode == LanguageMode.ES) return subtitle
        return when {
            subtitle.contains("Sarria a Santiago", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "The last 100 km from Sarria to Santiago de Compostela"
                LanguageMode.FR -> "Les 100 derniers km de Sarria à Saint-Jacques-de-Compostelle"
                LanguageMode.IT -> "Gli ultimi 100 km da Sarria a Santiago de Compostela"
                LanguageMode.DE -> "Die letzten 100 km von Sarria nach Santiago de Compostela"
                LanguageMode.NL -> "De laatste 100 km van Sarria naar Santiago de Compostela"
                LanguageMode.PL -> "Ostatnie 100 km z Sarria do Santiago de Compostela"
                LanguageMode.ZH -> "从萨里亚到圣地亚哥-德孔波斯特拉的最后 100 公里"
                LanguageMode.KO -> "사리아에서 산티아고 데 콤포스텔라까지 마지막 100km"
                LanguageMode.PT -> "Os últimos 100 km de Sarria a Santiago de Compostela"
                else -> subtitle
            }
            subtitle.contains("Saint-Jean-Pied-de-Port a Santiago", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "From Saint-Jean-Pied-de-Port to Santiago along the historic route"
                LanguageMode.FR -> "De Saint-Jean-Pied-de-Port à Saint-Jacques par la route jacobéenne historique"
                LanguageMode.IT -> "Da Saint-Jean-Pied-de-Port a Santiago lungo lo storico cammino"
                LanguageMode.DE -> "Von Saint-Jean-Pied-de-Port nach Santiago auf der historischen Route"
                LanguageMode.NL -> "Van Saint-Jean-Pied-de-Port naar Santiago via de historische route"
                LanguageMode.PL -> "Z Saint-Jean-Pied-de-Port do Santiago historycznym szlakiem"
                LanguageMode.ZH -> "从圣让-皮耶德波尔沿历史朝圣路线到圣地亚哥"
                LanguageMode.KO -> "생장피드포르에서 산티아고까지 역사적인 순례길을 따라"
                LanguageMode.PT -> "De Saint-Jean-Pied-de-Port a Santiago pela rota jacobeia histórica"
                else -> subtitle
            }
            subtitle.contains("primera ruta de peregrinación", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "The first pilgrimage route through the Astur-Galician mountains"
                LanguageMode.FR -> "Le premier itinéraire de pèlerinage à travers les montagnes asturo-galiciennes"
                LanguageMode.IT -> "Il primo cammino di pellegrinaggio attraverso le montagne astur-galiziane"
                LanguageMode.DE -> "Der erste Pilgerweg durch die asturisch-galicischen Berge"
                LanguageMode.NL -> "De eerste pelgrimsroute door de Asturisch-Galicische bergen"
                LanguageMode.PL -> "Pierwszy szlak pielgrzymkowy przez góry asturyjsko-galicyjskie"
                LanguageMode.ZH -> "穿越阿斯图里亚斯-加利西亚山区的首条朝圣路线"
                LanguageMode.KO -> "아스투리아스-갈리시아 산맥을 가로지르는 최초의 순례 경로"
                LanguageMode.PT -> "A primeira rota de peregrinação através das montanhas asturo-galegas"
                else -> subtitle
            }
            subtitle.contains("Ribadeo a Santiago", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "From Ribadeo to Santiago through inland Galicia"
                LanguageMode.FR -> "De Ribadeo à Saint-Jacques par l'intérieur de la Galice"
                LanguageMode.IT -> "Da Ribadeo a Santiago attraverso l'interno della Galizia"
                LanguageMode.DE -> "Von Ribadeo nach Santiago durch das galicische Binnenland"
                LanguageMode.NL -> "Van Ribadeo naar Santiago door het binnenland van Galicië"
                LanguageMode.PL -> "Z Ribadeo do Santiago przez wnętrze Galicji"
                LanguageMode.ZH -> "从里瓦德奥经加利西亚内陆到圣地亚哥"
                LanguageMode.KO -> "리바데오에서 내륙 갈리시아를 거쳐 산티아고까지"
                LanguageMode.PT -> "De Ribadeo a Santiago pelo interior da Galiza"
                else -> subtitle
            }
            subtitle.contains("Tui a Santiago", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "From Tui to Santiago among Albariño vineyards and Galician estuaries"
                LanguageMode.FR -> "De Tui à Saint-Jacques entre vignobles d'albariño et rias galiciennes"
                LanguageMode.IT -> "Da Tui a Santiago tra vigneti di albariño e rías galiziane"
                LanguageMode.DE -> "Von Tui nach Santiago zwischen Albariño-Weinbergen und galicischen Rias"
                LanguageMode.NL -> "Van Tui naar Santiago tussen Albariño-wijngaarden en Galicische ría's"
                LanguageMode.PL -> "Z Tui do Santiago wśród winnic albariño i galicyjskich zatok"
                LanguageMode.ZH -> "从图伊穿行于阿尔巴利诺葡萄园和加利西亚海湾前往圣地亚哥"
                LanguageMode.KO -> "투이에서 알바리뇨 포도밭과 갈리시아 만을 지나 산티아고까지"
                LanguageMode.PT -> "De Tui a Santiago entre vinhedos de albariño e rias galegas"
                else -> subtitle
            }
            subtitle.contains("Ferrol a Santiago", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "From Ferrol to Santiago along the Historic Route"
                LanguageMode.FR -> "De Ferrol à Saint-Jacques par le Tracé Historique"
                LanguageMode.IT -> "Da Ferrol a Santiago lungo il Tracciato Storico"
                LanguageMode.DE -> "Von Ferrol nach Santiago über die Historische Route"
                LanguageMode.NL -> "Van Ferrol naar Santiago via de Historische Route"
                LanguageMode.PL -> "Z Ferrol do Santiago szlakiem historycznym"
                LanguageMode.ZH -> "从费罗尔沿传统历史路线到圣地亚哥"
                LanguageMode.KO -> "페롤에서 산티아고까지 옛길을 따라"
                LanguageMode.PT -> "De Ferrol a Santiago pelo Traçado Antigo"
                else -> subtitle
            }
            subtitle.contains("Sevilla a Astorga", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "The great Roman causeway from Seville to Astorga"
                LanguageMode.FR -> "La grande voie romaine de Séville à Astorga"
                LanguageMode.IT -> "La grande strada romana da Siviglia ad Astorga"
                LanguageMode.DE -> "Die große römische Straße von Sevilla nach Astorga"
                LanguageMode.NL -> "De grote Romeinse heirbaan van Sevilla naar Astorga"
                LanguageMode.PL -> "Wielki rzymski trakt z Sewilli do Astorga"
                LanguageMode.ZH -> "从塞维利亚到阿斯托尔加的古罗马大道"
                LanguageMode.KO -> "세비야에서 아스토르가까지 로마 가도"
                LanguageMode.PT -> "A grande calçada romana de Sevilha a Astorga"
                else -> subtitle
            }
            subtitle.contains("Granja de Moreruela a Santiago", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "From Granja de Moreruela to Santiago across the mountains of Zamora and Ourense"
                LanguageMode.FR -> "De Granja de Moreruela à Saint-Jacques par les montagnes de Zamora et Ourense"
                LanguageMode.IT -> "Da Granja de Moreruela a Santiago attraverso le montagne di Zamora e Ourense"
                LanguageMode.DE -> "Von Granja de Moreruela nach Santiago über die Berge von Zamora und Ourense"
                LanguageMode.NL -> "Van Granja de Moreruela naar Santiago over de bergen van Zamora en Ourense"
                LanguageMode.PL -> "Z Granja de Moreruela do Santiago przez pasma górskie Zamora i Ourense"
                LanguageMode.ZH -> "从莫雷鲁埃拉庄园越过萨莫拉和奥伦塞山区前往圣地亚哥"
                LanguageMode.KO -> "그란하 데 모레루엘라에서 사모라와 오렌세 산악을 넘어 산티아고까지"
                LanguageMode.PT -> "De Granja de Moreruela a Santiago pelas serras de Zamora e Ourense"
                else -> subtitle
            }
            subtitle.contains("San Adrián", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "From Irun to Santo Domingo de la Calzada through the San Adrián tunnel"
                LanguageMode.FR -> "D'Irun à Santo Domingo de la Calzada par le tunnel de San Adrián"
                LanguageMode.IT -> "Da Irún a Santo Domingo de la Calzada attraverso il tunnel di San Adrián"
                LanguageMode.DE -> "Von Irún nach Santo Domingo de la Calzada durch den San-Adrián-Tunnel"
                LanguageMode.NL -> "Van Irún naar Santo Domingo de la Calzada via de San Adrián tunnel"
                LanguageMode.PL -> "Z Irún do Santo Domingo de la Calzada przez tunel San Adrián"
                LanguageMode.ZH -> "从伊伦穿过圣阿德里安隧道前往圣多明各-德拉卡尔萨达"
                LanguageMode.KO -> "이룬에서 산 아드리안 터널을 거쳐 산토 도밍고 데 라 칼사다까지"
                LanguageMode.PT -> "De Irún a Santo Domingo de la Calzada pelo túnel de San Adrián"
                else -> subtitle
            }
            subtitle.contains("valle atlántico de Baztán", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "From Bayonne to Pamplona across the Atlantic Baztán valley"
                LanguageMode.FR -> "De Bayonne à Pampelune à travers la vallée atlantique du Baztán"
                LanguageMode.IT -> "Da Bayonne a Pamplona attraverso la valle atlantica di Baztán"
                LanguageMode.DE -> "Von Bayonne nach Pamplona durch das atlantische Baztán-Tal"
                LanguageMode.NL -> "Van Bayonne naar Pamplona door de Atlantische Baztán-vallei"
                LanguageMode.PL -> "Z Bajonny do Pampeluny przez atlantycką dolinę Baztán"
                LanguageMode.ZH -> "从巴约讷穿过大西洋巴兹坦山谷前往潘普洛纳"
                LanguageMode.KO -> "바욘에서 바스탄 계곡을 거쳐 팜플로나까지"
                LanguageMode.PT -> "De Baiona a Pamplona pelo vale atlântico de Baztán"
                else -> subtitle
            }
            subtitle.contains("puerto de Pajares", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "From León to Oviedo across the Cantabrian range and Pajares pass"
                LanguageMode.FR -> "De León à Oviedo par la cordillère cantabrique et le col de Pajares"
                LanguageMode.IT -> "Da León a Oviedo attraverso la cordigliera cantabrica e il passo di Pajares"
                LanguageMode.DE -> "Von León nach Oviedo über das Kantabrische Gebirge und den Pajares-Pass"
                LanguageMode.NL -> "Van León naar Oviedo over het Cantabrisch gebergte en de Pajares-pas"
                LanguageMode.PL -> "Z León do Oviedo przez Góry Kantabryjskie i przełęcz Pajares"
                LanguageMode.ZH -> "从莱昂穿过坎塔布连山脉和帕哈雷斯山口前往奥维耶多"
                LanguageMode.KO -> "레온에서 파하레스 고개를 넘어 오비에도까지"
                LanguageMode.PT -> "De León a Oviedo pela cordilheira cantábrica e pelo porto de Pajares"
                else -> subtitle
            }
            subtitle.contains("Fin del Mundo y la Costa da Morte", ignoreCase = true) -> when (mode) {
                LanguageMode.EN -> "The epilogue to the End of the World and the Death Coast"
                LanguageMode.FR -> "L'épilogue vers le Bout du Monde et la Côte de la Mort"
                LanguageMode.IT -> "L'epilogo verso la Fine del Mondo e la Costa da Morte"
                LanguageMode.DE -> "Der Epilog zum Ende der Welt und der Todesküste"
                LanguageMode.NL -> "Het epiloog naar het Einde van de Wereld en de Costa da Morte"
                LanguageMode.PL -> "Epilog ku Końcowi Świata i Wybrzeżu Śmierci"
                LanguageMode.ZH -> "通往世界尽头与死亡海岸的尾声"
                LanguageMode.KO -> "세상의 끝과 죽음의 해안으로 향하는 에필로그"
                LanguageMode.PT -> "O epílogo até ao Fim do Mundo e à Costa da Morte"
                else -> subtitle
            }
            else -> subtitle
        }
    }

    private val stageTitleRegex = Regex("""^(?:Etapa|Stage|Étape|Tappa|Etappe|Etap|第)\s*(\d+)[:：\s]*(.*?)(?:\s+(?:a|to|à|nach|naar|do|至)\s+)(.*)$""", RegexOption.IGNORE_CASE)

    fun getLocalizedStageTitle(stageTitle: String, mode: LanguageMode): String {
        val match = stageTitleRegex.find(stageTitle) ?: return stageTitle
        val num = match.groupValues[1]
        val origin = match.groupValues[2].trim()
        val dest = match.groupValues[3].trim()
        return when (mode) {
            LanguageMode.ES -> "Etapa $num: $origin a $dest"
            LanguageMode.EN -> "Stage $num: $origin to $dest"
            LanguageMode.FR -> "Étape $num : $origin à $dest"
            LanguageMode.IT -> "Tappa $num: $origin a $dest"
            LanguageMode.DE -> "Etappe $num: $origin nach $dest"
            LanguageMode.NL -> "Etappe $num: $origin naar $dest"
            LanguageMode.PL -> "Etap $num: $origin do $dest"
            LanguageMode.ZH -> "第 $num 赛段：$origin 至 $dest"
            LanguageMode.KO -> "제 $num 구간: $origin ~ $dest"
            LanguageMode.PT -> "Etapa $num: $origin a $dest"
        }
    }

    fun getLocalizedDifficulty(diff: String, mode: LanguageMode): String {
        val clean = diff.trim().lowercase()
        return when {
            clean.contains("muy") || clean.contains("very") || clean.contains("sehr") || clean.contains("très") || clean.contains("molto") || clean.contains("zeer") || clean.contains("bardzo") || clean.contains("极") || clean.contains("schwer") || clean.contains("difficult") -> when (mode) {
                LanguageMode.ES -> "Muy Difícil"
                LanguageMode.EN -> "Very Difficult"
                LanguageMode.FR -> "Très difficile"
                LanguageMode.IT -> "Molto difficile"
                LanguageMode.DE -> "Sehr schwer"
                LanguageMode.NL -> "Zeer moeilijk"
                LanguageMode.PL -> "Bardzo trudny"
                LanguageMode.ZH -> "非常困难"
                LanguageMode.KO -> "매우 어려움"
                LanguageMode.PT -> "Muito Difícil"
            }
            clean.contains("exig") || clean.contains("demand") || clean.contains("anspruch") || clean.contains("impeg") || clean.contains("veeleis") || clean.contains("wymag") || clean.contains("挑战") || clean.contains("difícil") || clean.contains("dificil") -> when (mode) {
                LanguageMode.ES -> "Exigente"
                LanguageMode.EN -> "Demanding"
                LanguageMode.FR -> "Exigeante"
                LanguageMode.IT -> "Impegnativa"
                LanguageMode.DE -> "Anspruchsvoll"
                LanguageMode.NL -> "Veeleisend"
                LanguageMode.PL -> "Wymagający"
                LanguageMode.ZH -> "具有挑战性"
                LanguageMode.KO -> "어려움"
                LanguageMode.PT -> "Exigente"
            }
            clean.contains("mod") || clean.contains("media") || clean.contains("medio") || clean.contains("mäßig") || clean.contains("gemid") || clean.contains("umiark") || clean.contains("中") || clean.contains("보통") -> when (mode) {
                LanguageMode.ES -> "Moderado"
                LanguageMode.EN -> "Moderate"
                LanguageMode.FR -> "Modéré"
                LanguageMode.IT -> "Moderato"
                LanguageMode.DE -> "Mäßig"
                LanguageMode.NL -> "Gemiddeld"
                LanguageMode.PL -> "Umiarkowany"
                LanguageMode.ZH -> "中等"
                LanguageMode.KO -> "보통"
                LanguageMode.PT -> "Moderado"
            }
            clean.contains("fác") || clean.contains("fac") || clean.contains("eas") || clean.contains("leich") || clean.contains("makk") || clean.contains("łatw") || clean.contains("简") || clean.contains("쉬움") -> when (mode) {
                LanguageMode.ES -> "Fácil"
                LanguageMode.EN -> "Easy"
                LanguageMode.FR -> "Facile"
                LanguageMode.IT -> "Facile"
                LanguageMode.DE -> "Leicht"
                LanguageMode.NL -> "Makkelijk"
                LanguageMode.PL -> "Łatwy"
                LanguageMode.ZH -> "简单"
                LanguageMode.KO -> "쉬움"
                LanguageMode.PT -> "Fácil"
            }
            else -> diff
        }
    }

    fun findNotesByOriginDest(origin: String, destination: String): String? {
        val o = origin.trim().lowercase()
        val d = destination.trim().lowercase()
        for (route in ROUTES) {
            for (stage in route.stages) {
                if (stage.origin.trim().lowercase() == o && stage.destination.trim().lowercase() == d) {
                    return stage.notes
                }
            }
        }
        return null
    }

    fun isAnyPredefinedNote(notes: String): Boolean {
        val clean = notes.trim()
        for (route in ROUTES) {
            for (stage in route.stages) {
                if (stage.notes.trim() == clean) return true
            }
        }
        return false
    }

    fun findStageByNotes(notes: String): PredefinedStageInfo? {
        val clean = notes.trim()
        for (route in ROUTES) {
            for (stage in route.stages) {
                if (stage.notes.trim() == clean) return stage
            }
        }
        return null
    }

    fun getLocalizedNotes(
        notes: String,
        mode: LanguageMode,
        origin: String = "",
        destination: String = ""
    ): String {
        return com.example.data.sample.notes.PredefinedNotesRepository.getLocalizedNotes(
            notes = notes,
            origin = origin,
            destination = destination,
            mode = mode
        )
    }

    fun getLocalizedRouteInfo(route: PredefinedRouteInfo, mode: LanguageMode): PredefinedRouteInfo {
        return route.copy(
            title = getLocalizedTitle(route.title, mode),
            subtitle = getLocalizedSubtitle(route.subtitle, mode),
            note = route.note,
            stages = route.stages.map { stage ->
                val localizedNotes = com.example.data.sample.notes.PredefinedNotesRepository.getStageNotes(
                    stage.origin,
                    stage.destination,
                    mode
                ) ?: getLocalizedNotes(stage.notes, mode, stage.origin, stage.destination)
                stage.copy(
                    title = getLocalizedStageTitle(stage.title, mode),
                    difficulty = getLocalizedDifficulty(stage.difficulty, mode),
                    notes = localizedNotes
                )
            }
        )
    }
}
