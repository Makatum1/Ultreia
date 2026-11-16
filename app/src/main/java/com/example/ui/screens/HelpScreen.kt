package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.backup.BackupManager
import com.example.ui.CaminoViewModel
import com.example.ui.settings.LanguageMode
import com.example.ui.theme.*

data class FaqCategory(
    val key: String,
    val labels: Map<LanguageMode, String>
) {
    fun label(mode: LanguageMode): String = labels[mode] ?: labels[LanguageMode.EN] ?: labels[LanguageMode.ES] ?: key
}

data class FaqItem(
    val id: String,
    val categoryKey: String,
    val questions: Map<LanguageMode, String>,
    val answers: Map<LanguageMode, String>
) {
    fun question(mode: LanguageMode): String = questions[mode] ?: questions[LanguageMode.EN] ?: questions[LanguageMode.ES] ?: ""
    fun answer(mode: LanguageMode): String = answers[mode] ?: answers[LanguageMode.EN] ?: answers[LanguageMode.ES] ?: ""
}

val FAQ_CATEGORIES = listOf(
    FaqCategory("gpx", mapOf(
        LanguageMode.ES to "GPX y Mapas",
        LanguageMode.EN to "GPX & Maps",
        LanguageMode.FR to "GPX et Cartes",
        LanguageMode.IT to "GPX e Mappe",
        LanguageMode.ZH to "GPX 与地图",
        LanguageMode.NL to "GPX en Kaarten",
        LanguageMode.DE to "GPX & Karten",
        LanguageMode.PL to "GPX i Mapy"
    )),
    FaqCategory("stages", mapOf(
        LanguageMode.ES to "Etapas y Rutas",
        LanguageMode.EN to "Stages & Routes",
        LanguageMode.FR to "Étapes et Itinéraires",
        LanguageMode.IT to "Tappe e Percorsi",
        LanguageMode.ZH to "赛段与路线",
        LanguageMode.NL to "Etappes en Routes",
        LanguageMode.DE to "Etappen & Routen",
        LanguageMode.PL to "Etapy i Szlaki"
    )),
    FaqCategory("accommodations", mapOf(
        LanguageMode.ES to "Alojamientos",
        LanguageMode.EN to "Accommodations",
        LanguageMode.FR to "Hébergements",
        LanguageMode.IT to "Alloggi",
        LanguageMode.ZH to "住宿预订",
        LanguageMode.NL to "Accommodaties",
        LanguageMode.DE to "Unterkünfte",
        LanguageMode.PL to "Noclegi"
    )),
    FaqCategory("journal", mapOf(
        LanguageMode.ES to "Vivencias y Fotos",
        LanguageMode.EN to "Journal & Photos",
        LanguageMode.FR to "Souvenirs et Photos",
        LanguageMode.IT to "Ricordi e Foto",
        LanguageMode.ZH to "回忆与照片",
        LanguageMode.NL to "Herinneringen & Foto's",
        LanguageMode.DE to "Erlebnisse & Fotos",
        LanguageMode.PL to "Wspomnienia i Zdjęcia"
    )),
    FaqCategory("agenda", mapOf(
        LanguageMode.ES to "Viajes y Agenda",
        LanguageMode.EN to "Trips & Agenda",
        LanguageMode.FR to "Voyages et Agenda",
        LanguageMode.IT to "Viaggi e Agenda",
        LanguageMode.ZH to "旅程与日程",
        LanguageMode.NL to "Reizen en Agenda",
        LanguageMode.DE to "Reisen & Programm",
        LanguageMode.PL to "Wyprawy i Harmonogram"
    )),
    FaqCategory("stats", mapOf(
        LanguageMode.ES to "Estadísticas",
        LanguageMode.EN to "Statistics",
        LanguageMode.FR to "Statistiques",
        LanguageMode.IT to "Statistiche",
        LanguageMode.ZH to "数据统计",
        LanguageMode.NL to "Statistieken",
        LanguageMode.DE to "Statistiken",
        LanguageMode.PL to "Statystyki"
    )),
    FaqCategory("rewards", mapOf(
        LanguageMode.ES to "Recompensas",
        LanguageMode.EN to "Rewards",
        LanguageMode.FR to "Récompenses",
        LanguageMode.IT to "Premi",
        LanguageMode.ZH to "奖励与成就",
        LanguageMode.NL to "Beloningen",
        LanguageMode.DE to "Belohnungen",
        LanguageMode.PL to "Nagrody"
    )),
    FaqCategory("pdf", mapOf(
        LanguageMode.ES to "Exportación y PDF",
        LanguageMode.EN to "Export & PDF",
        LanguageMode.FR to "Exportation et PDF",
        LanguageMode.IT to "Esportazione e PDF",
        LanguageMode.ZH to "导出与 PDF",
        LanguageMode.NL to "Export & PDF",
        LanguageMode.DE to "Export & PDF",
        LanguageMode.PL to "Eksport i PDF"
    )),
    FaqCategory("backup", mapOf(
        LanguageMode.ES to "Copias y Datos",
        LanguageMode.EN to "Backups & Data",
        LanguageMode.FR to "Sauvegardes et Données",
        LanguageMode.IT to "Backup e Dati",
        LanguageMode.ZH to "备份与数据",
        LanguageMode.NL to "Back-ups & Gegevens",
        LanguageMode.DE to "Backups & Daten",
        LanguageMode.PL to "Kopie i Dane"
    )),
    FaqCategory("profiles", mapOf(
        LanguageMode.ES to "Perfiles y Portada",
        LanguageMode.EN to "Profiles & Cover",
        LanguageMode.FR to "Profils et Couverture",
        LanguageMode.IT to "Profili e Copertina",
        LanguageMode.ZH to "资料与封面",
        LanguageMode.NL to "Profielen & Omslag",
        LanguageMode.DE to "Profile & Titelbild",
        LanguageMode.PL to "Profile i Okładka"
    )),
    FaqCategory("settings", mapOf(
        LanguageMode.ES to "Ajustes y Consejos",
        LanguageMode.EN to "Settings & Tips",
        LanguageMode.FR to "Paramètres et Conseils",
        LanguageMode.IT to "Impostazioni e Consigli",
        LanguageMode.ZH to "设置与贴士",
        LanguageMode.NL to "Instellingen & Tips",
        LanguageMode.DE to "Einstellungen & Tipps",
        LanguageMode.PL to "Ustawienia i Wskazówki"
    ))
)

val INITIAL_FAQS = listOf(
    // 1. GPX y Mapas
    FaqItem(
        id = "faq_gpx_import",
        categoryKey = "gpx",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo importar un archivo GPX a una etapa o día?",
            LanguageMode.EN to "How to import a GPX file into a stage or day?",
            LanguageMode.FR to "Comment importer un fichier GPX dans une étape ou une journée ?",
            LanguageMode.IT to "Come importare un file GPX in una tappa o giorno?",
            LanguageMode.ZH to "如何将 GPX 文件导入赛段或日程？",
            LanguageMode.NL to "Hoe importeer je een GPX-bestand in een etappe of dag?",
            LanguageMode.DE to "Wie importiert man eine GPX-Datei in eine Etappe oder einen Tag?",
            LanguageMode.PL to "Jak zaimportować plik GPX do etapu lub dnia?"
        ),
        answers = mapOf(
            LanguageMode.ES to "Accede al detalle de la etapa o día. En la tarjeta del mapa o en el menú de opciones (tres puntos), pulsa en 'Subir o Actualizar GPX'. Selecciona el archivo .gpx desde tu dispositivo para calcular distancia, desniveles y trazar la ruta en 2D.",
            LanguageMode.EN to "Open the stage or day details. On the map card or options menu (three dots), tap 'Upload or Update GPX'. Select the .gpx file from your device to automatically calculate distance, elevation gain/loss and draw the 2D route.",
            LanguageMode.FR to "Accédez aux détails de l'étape. Sur la carte ou le menu options, appuyez sur 'Importer GPX'. Sélectionnez le fichier .gpx pour calculer distance, dénivelé et tracer la route en 2D.",
            LanguageMode.IT to "Apri i dettagli della tappa. Sulla scheda mappa o menu opzioni, tocca 'Carica GPX'. Seleziona il file .gpx per calcolare distanza, dislivello e tracciare il percorso in 2D.",
            LanguageMode.ZH to "打开赛段详情页，在地图卡片或选项菜单中点击“上传或更新 GPX”，选择 .gpx 文件后系统将自动计算距离、起伏海拔并在 2D 地图上绘制轨迹。",
            LanguageMode.NL to "Open de etappedetails. Tik op de kaart of het menu op 'GPX Uploaden'. Kies het .gpx-bestand om afstand en hoogtemeters te berekenen en de route in 2D te tekenen.",
            LanguageMode.DE to "Öffnen Sie die Etappendetails. Tippen Sie auf der Karte auf 'GPX Hochladen'. Wählen Sie die .gpx-Datei, um Distanz, Höhenmeter zu berechnen und die Route in 2D darzustellen.",
            LanguageMode.PL to "Wejdź w szczegóły etapu. Na karcie mapy wybierz 'Prześlij GPX'. Wybierz plik .gpx ze swojego urządzenia, aby automatycznie obliczyć dystans, przewyższenia i narysować trasę 2D."
        )
    ),
    FaqItem(
        id = "faq_map_layers",
        categoryKey = "gpx",
        questions = mapOf(
            LanguageMode.ES to "¿Qué capas de mapa están disponibles y cómo cambiarlas?",
            LanguageMode.EN to "What map layers are available and how to switch between them?",
            LanguageMode.FR to "Quelles couches de carte sont disponibles et comment les changer ?",
            LanguageMode.IT to "Quali livelli di mappa sono disponibili e come cambiarli?",
            LanguageMode.ZH to "有哪些地图图层可用，如何进行切换？",
            LanguageMode.NL to "Welke kaartlagen zijn beschikbaar en hoe wissel je ertussen?",
            LanguageMode.DE to "Welche Kartenebenen gibt es und wie wechselt man sie?",
            LanguageMode.PL to "Jakie warstwy mapy są dostępne i jak je przełączać?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En la pestaña 'Mapa', pulsa el botón de capas en la esquina superior. Puedes alternar entre Callejero (Google Roadmap), Satélite, Relieve topográfico y OpenStreetMap.",
            LanguageMode.EN to "In the 'Map' tab, tap the layers button in the upper corner. You can toggle between Roadmap (Google Roadmap), Satellite, Terrain, and OpenStreetMap.",
            LanguageMode.FR to "Dans l'onglet 'Carte', appuyez sur le bouton des calques. Basculez entre Plan (Google Roadmap), Satellite, Relief et OpenStreetMap.",
            LanguageMode.IT to "Nella scheda 'Mappa', tocca il pulsante dei livelli. Puoi alternare tra Mappa Stradale, Satellite, Rilievo e OpenStreetMap.",
            LanguageMode.ZH to "在“地图”标签页中点击右上角的图层按钮，可在街道地图（Google Roadmap）、卫星影像、地形起伏以及 OpenStreetMap 之间自由切换。",
            LanguageMode.NL to "Tik in het tabblad 'Kaart' op de lagenknop. Schakel tussen Wegenkaart (Google Roadmap), Satelliet, Terrein en OpenStreetMap.",
            LanguageMode.DE to "Tippen Sie im Tab 'Karte' auf die Ebenen-Schaltfläche. Wechseln Sie zwischen Straßenkarte, Satellit, Gelände und OpenStreetMap.",
            LanguageMode.PL to "W zakładce 'Mapa' dotknij przycisku warstw w górnym rogu. Możesz przełączać między Planem ulic (Google Roadmap), Satelitą, Rzeźbą terenu i OpenStreetMap."
        )
    ),
    FaqItem(
        id = "faq_map_navigation",
        categoryKey = "gpx",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo navegar una ruta o abrirla en Google Maps?",
            LanguageMode.EN to "How to navigate a route or open it in Google Maps?",
            LanguageMode.FR to "Comment naviguer un itinéraire dans Google Maps ?",
            LanguageMode.IT to "Come navigare un itinerario in Google Maps?",
            LanguageMode.ZH to "如何在 Google 地图中导航或查看路线？",
            LanguageMode.NL to "Hoe navigeer je een route in Google Maps?",
            LanguageMode.DE to "Wie navigiert man eine Route in Google Maps?",
            LanguageMode.PL to "Jak nawigować trasę lub otworzyć ją w Mapach Google?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En el detalle de la etapa pulsa 'Abrir en Mapas' para consultar el entorno o 'Navegar Ruta en Mapas' para iniciar la navegación GPS paso a paso.",
            LanguageMode.EN to "In the stage details, tap 'Open in Maps' to inspect points of interest or 'Navigate Route in Maps' to launch turn-by-turn GPS guidance.",
            LanguageMode.FR to "Dans les détails de l'étape, appuyez sur 'Ouvrir dans Maps' ou 'Naviguer l'Itinéraire' pour lancer le guidage GPS.",
            LanguageMode.IT to "Nei dettagli della tappa, tocca 'Apri in Maps' o 'Naviga Percorso' per avviare la navigazione GPS dettagliata.",
            LanguageMode.ZH to "在赛段详情页中，点击“在地图中打开”可查看周边环境，点击“在地图中导航路线”可直接启动 GPS 逐向路线导航。",
            LanguageMode.NL to "Tik in de etappedetails op 'Openen in Maps' of 'Route Navigeren' om turn-by-turn gps-begeleiding te starten.",
            LanguageMode.DE to "Tippen Sie in den Etappendetails auf 'In Maps Öffnen' oder 'Route Navigieren', um die GPS-Navigation zu starten.",
            LanguageMode.PL to "W szczegółach etapu wybierz 'Otwórz w Mapach' lub 'Nawiguj Trasę w Mapach', aby uruchomić dokładną nawigację GPS krok po kroku."
        )
    ),
    FaqItem(
        id = "faq_map_gps_center",
        categoryKey = "gpx",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo centrar el mapa en mi ubicación actual mientras camino?",
            LanguageMode.EN to "How to center the map on my current location while hiking?",
            LanguageMode.FR to "Comment centrer la carte sur ma position actuelle en marchant ?",
            LanguageMode.IT to "Come centrare la mappa sulla mia posizione attuale mentre cammino?",
            LanguageMode.ZH to "徒步过程中如何将地图居中到我的当前位置？",
            LanguageMode.NL to "Hoe centreer je de kaart op je huidige locatie tijdens het wandelen?",
            LanguageMode.DE to "Wie zentriert man die Karte auf den aktuellen Standort beim Wandern?",
            LanguageMode.PL to "Jak wyśrodkować mapę na mojej aktualnej lokalizacji podczas wędrówki?"
        ),
        answers = mapOf(
            LanguageMode.ES to "Pulsa el botón flotante circular con icono de GPS en la esquina inferior del mapa. La app solicitará permiso de localización y centrará la vista sobre tus coordenadas.",
            LanguageMode.EN to "Tap the round floating GPS button in the lower corner of the map. The app requests location permission and instantly centers the view on your coordinates.",
            LanguageMode.FR to "Appuyez sur le bouton GPS flottant en bas de la carte pour centrer immédiatement la vue sur votre position géographique.",
            LanguageMode.IT to "Tocca il pulsante GPS circolare in basso sulla mappa per centrare immediatamente la visuale sulla tua posizione attuale.",
            LanguageMode.ZH to "点击地图右下角的圆形浮动 GPS 定位按钮，授予权限后地图将立即定位并居中显示您的实时坐标。",
            LanguageMode.NL to "Tik op de ronde zwevende gps-knop onderaan de kaart om de weergave direct op jouw realtime locatie te centreren.",
            LanguageMode.DE to "Tippen Sie auf die runde GPS-Schaltfläche unten auf der Karte, um die Ansicht sofort auf Ihren aktuellen Standort zu zentrieren.",
            LanguageMode.PL to "Dotknij okrągłego przycisku GPS w dolnym rogu mapy. Aplikacja poprosi o uprawnienia i wyśrodkuje widok na Twoich współrzędnych."
        )
    ),
    FaqItem(
        id = "faq_map_scope",
        categoryKey = "gpx",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo ver todos los tracks de mi ruta juntos en el mapa?",
            LanguageMode.EN to "How to view all tracks of my route together on the map?",
            LanguageMode.FR to "Comment voir toutes les traces de mon itinéraire ensemble sur la carte ?",
            LanguageMode.IT to "Come vedere tutte le tracce del mio itinerario insieme sulla mappa?",
            LanguageMode.ZH to "如何在地图上同时查看该路线的所有 GPX 轨迹？",
            LanguageMode.NL to "Hoe bekijk je alle tracks van je route samen op de kaart?",
            LanguageMode.DE to "Wie sieht man alle Tracks der Route zusammen auf der Karte?",
            LanguageMode.PL to "Jak zobaczyć wszystkie ślady mojej trasy razem na mapie?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En la barra superior de 'Mapa', usa el menú central y selecciona 'Total Caminos' o 'Total Viajes'. El mapa encuadrará en conjunto todos los archivos GPX vinculados.",
            LanguageMode.EN to "In the top bar of 'Map', use the dropdown selector and pick 'Total Caminos' or 'Total Trips' to display and frame all linked GPX tracks at once.",
            LanguageMode.FR to "Dans la barre supérieure de 'Carte', sélectionnez 'Total Chemins' ou 'Total Voyages' pour afficher l'ensemble des traces GPX associées.",
            LanguageMode.IT to "Nella barra superiore di 'Mappa', seleziona 'Totale Cammini' o 'Totale Viaggi' per mostrare e inquadrare tutte le tracce GPX collegate.",
            LanguageMode.ZH to "在“地图”顶部导航栏中，点击中间选择菜单并选择“全部朝圣之路”或“全部旅程”，地图即可同屏整合显示所有已关联的 GPX 轨迹。",
            LanguageMode.NL to "Gebruik in de bovenbalk van 'Kaart' de keuzeknop en selecteer 'Totaal Camino's' of 'Totaal Reizen' om alle gekoppelde GPX-tracks samen te zien.",
            LanguageMode.DE to "Wählen Sie in der oberen Leiste von 'Karte' die Option 'Alle Wege' oder 'Alle Reisen', um alle verknüpften GPX-Tracks gemeinsam anzuzeigen.",
            LanguageMode.PL to "Na górnym pasku 'Mapy' wybierz opcję 'Wszystkie Szlaki' lub 'Wszystkie Wyprawy', aby wyświetlić jednocześnie wszystkie powiązane ślady GPX."
        )
    ),

    // 2. Etapas y Rutas
    FaqItem(
        id = "faq_new_stage",
        categoryKey = "stages",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo añado una nueva etapa o día de viaje?",
            LanguageMode.EN to "How to add a new stage or travel day?",
            LanguageMode.FR to "Comment ajouter une nouvelle étape ou journée ?",
            LanguageMode.IT to "Come aggiungere una nuova tappa o giorno di viaggio?",
            LanguageMode.ZH to "如何添加新的赛段或旅行日程？",
            LanguageMode.NL to "Hoe voeg je een nieuwe etappe of reisdag toe?",
            LanguageMode.DE to "Wie fügt man eine neue Etappe oder einen Reisetag hinzu?",
            LanguageMode.PL to "Jak dodać nowy etap lub dzień podróży?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En la pestaña 'Etapas' (o 'Días' en Viajes), pulsa el botón flotante circular con el signo '+' en la esquina inferior derecha e introduce los datos del itinerario.",
            LanguageMode.EN to "In the 'Stages' tab (or 'Days' in Trips), tap the floating '+' button in the bottom right corner and enter the itinerary details.",
            LanguageMode.FR to "Dans l'onglet 'Étapes' (ou 'Jours'), appuyez sur le bouton '+' en bas à droite et renseignez les détails de votre journée.",
            LanguageMode.IT to "Nella scheda 'Tappe' (o 'Giorni'), tocca il pulsante '+' in basso a destra e compila i dettagli dell'itinerario.",
            LanguageMode.ZH to "在“赛段”（或旅行模式下的“日程”）标签页中，点击右下角的圆形“+”浮动按钮，在弹出的窗口中填入起终点、距离与预估难度即可。",
            LanguageMode.NL to "Tik in het tabblad 'Etappes' (of 'Dagen') op de zwevende knop '+' rechtsonder en vul de reisgegevens in.",
            LanguageMode.DE to "Tippen Sie im Tab 'Etappen' (oder 'Tage') auf die Schaltfläche '+' unten rechts und geben Sie die Reisedaten ein.",
            LanguageMode.PL to "W zakładce 'Etapy' (lub 'Dni' w profilu Wypraw) naciśnij okrągły przycisk '+' w prawym dolnym rogu i wprowadź dane trasy."
        )
    ),
    FaqItem(
        id = "faq_cascade_dates",
        categoryKey = "stages",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo funciona la actualización de fechas en cascada?",
            LanguageMode.EN to "How does cascading date update work?",
            LanguageMode.FR to "Comment fonctionne la mise à jour des dates en cascade ?",
            LanguageMode.IT to "Come funziona l'aggiornamento a cascata delle date?",
            LanguageMode.ZH to "级联日期更新功能是如何工作的？",
            LanguageMode.NL to "Hoe werkt het trapsgewijs bijwerken van datums?",
            LanguageMode.DE to "Wie funktioniert die kaskadierende Datumsaktualisierung?",
            LanguageMode.PL to "Jak działa kaskadowa aktualizacja dat?"
        ),
        answers = mapOf(
            LanguageMode.ES to "Al cambiar la fecha de una etapa intermedia, la app te preguntará si deseas actualizar en cascada. Si aceptas, reajustará automáticamente las fechas de las etapas posteriores.",
            LanguageMode.EN to "When editing the date of an intermediate stage, the app asks if you want a cascade update. If accepted, subsequent stages adjust their dates automatically.",
            LanguageMode.FR to "En modifiant la date d'une étape, l'application propose d'ajuster en cascade les dates de toutes les étapes suivantes.",
            LanguageMode.IT to "Se modifichi la data di una tappa, l'app ti chiederà se aggiornare a cascata le date successive per mantenere la sequenza corretta.",
            LanguageMode.ZH to "当修改某个中间赛段的日期时，系统会智能提示是否进行级联更新；若确认，后续所有赛段的日期将自动顺延调整，免去逐个修改的繁琐。",
            LanguageMode.NL to "Wanneer je de datum van een tussenliggende etappe wijzigt, vraagt de app of je volgende data automatisch wilt doorschuiven.",
            LanguageMode.DE to "Wenn Sie das Datum einer Etappe ändern, fragt die App, ob Folgetermine automatisch kaskadierend angepasst werden sollen.",
            LanguageMode.PL to "Przy zmianie daty etapu aplikacja zapyta o aktualizację kaskadową. Po zatwierdzeniu automatycznie przesunie daty wszystkich kolejnych etapów."
        )
    ),
    FaqItem(
        id = "faq_mark_completed",
        categoryKey = "stages",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo marcar una etapa como completada y para qué sirve?",
            LanguageMode.EN to "How to mark a stage as completed and what is it for?",
            LanguageMode.FR to "Comment marquer une étape comme terminée et à quoi cela sert-il ?",
            LanguageMode.IT to "Come segnare una tappa come completata e a cosa serve?",
            LanguageMode.ZH to "如何将赛段标记为已完成，有何作用？",
            LanguageMode.NL to "Hoe markeer je een etappe als voltooid en waarvoor dient dit?",
            LanguageMode.DE to "Wie markiert man eine Etappe als abgeschlossen und wozu dient das?",
            LanguageMode.PL to "Jak oznaczyć etap jako ukończony i do czego to służy?"
        ),
        answers = mapOf(
            LanguageMode.ES to "Pulsa 'Marcar Hecha'. La etapa lucirá un distintivo verde, sus km y desniveles se sumarán al balance general y la barra de progreso avanzará hacia el 100%.",
            LanguageMode.EN to "Tap 'Mark as Done'. The stage gets a green badge, its distance and elevation credit towards your balance, and total progress advances towards 100%.",
            LanguageMode.FR to "Appuyez sur 'Marquer comme Faite'. L'étape devient verte, ses kilomètres s'ajoutent au bilan et votre progression se rapproche de 100%.",
            LanguageMode.IT to "Tocca 'Segna come Fatta'. La tappa mostrerà un badge verde, la distanza verrà aggiunta al bilancio e la barra di avanzamento salirà al 100%.",
            LanguageMode.ZH to "点击“标记为已完成”，该赛段将显示绿色完成徽章，其里程与海拔将计入已完成统计，总体进度条亦将同步迈向 100%。",
            LanguageMode.NL to "Tik op 'Markeren als Gedaan'. De etappe krijgt een groene badge, kilometers tellen mee voor je balans en de voortgangsbalk stijgt naar 100%.",
            LanguageMode.DE to "Tippen Sie auf 'Als Erledigt Markieren'. Die Etappe wird grün markiert, ihre Kilometer werden verbucht und der Fortschrittsbalken nähert sich 100%.",
            LanguageMode.PL to "Kliknij 'Oznacz jako Wykonane'. Etap otrzyma zielony znaczek, jego kilometry zostaną doliczone do bilansu, a pasek postępu zbliży się do 100%."
        )
    ),

    // 3. Alojamientos y Gastos
    FaqItem(
        id = "faq_accommodations",
        categoryKey = "accommodations",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo registrar alojamientos y reservas?",
            LanguageMode.EN to "How to manage accommodations and bookings?",
            LanguageMode.FR to "Comment enregistrer des hébergements et réservations ?",
            LanguageMode.IT to "Come gestire alloggi e prenotazioni?",
            LanguageMode.ZH to "如何记录和管理住宿与预订信息？",
            LanguageMode.NL to "Hoe beheer je accommodaties en boekingen?",
            LanguageMode.DE to "Wie verwaltet man Unterkünfte und Buchungen?",
            LanguageMode.PL to "Jak zarządzać noclegami i rezerwacjami?"
        ),
        answers = mapOf(
            LanguageMode.ES to "Dentro del detalle de la etapa, pulsa en 'Añadir Alojamiento'. Registra nombre, dirección, teléfono, opciones de habitación y precios por noche.",
            LanguageMode.EN to "Inside stage details, tap 'Add Accommodation'. You can save property name, address, telephone, room types, and nightly rates.",
            LanguageMode.FR to "Dans le détail de l'étape, cliquez sur 'Ajouter un Hébergement' pour enregistrer nom, adresse, téléphone, types de chambres et tarifs.",
            LanguageMode.IT to "Nei dettagli della tappa, tocca 'Aggiungi Alloggio'. Puoi inserire nome, indirizzo, telefono, tipologie di camere e tariffe.",
            LanguageMode.ZH to "在赛段详情页内点击“添加住宿”，可记录酒店或旅馆名称、详细地址、联系电话、房型选项及每晚价格。",
            LanguageMode.NL to "Tik in de etappedetails op 'Accommodatie Toevoegen'. Noteer naam, adres, telefoonnummer, kamertypes en prijzen per nacht.",
            LanguageMode.DE to "Tippen Sie in den Etappendetails auf 'Unterkunft Hinzufügen', um Name, Adresse, Telefonnummer, Zimmerarten und Preise zu speichern.",
            LanguageMode.PL to "W szczegółach etapu dotknij 'Dodaj Nocleg'. Możesz zapisać nazwę, adres, telefon, rodzaje pokoi i ceny za nocleg."
        )
    ),
    FaqItem(
        id = "faq_call_accommodation",
        categoryKey = "accommodations",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo llamar por teléfono directamente a un alojamiento?",
            LanguageMode.EN to "How to call an accommodation directly from the app?",
            LanguageMode.FR to "Comment appeler directement un hébergement ?",
            LanguageMode.IT to "Come telefonare direttamente a un alloggio dall'app?",
            LanguageMode.ZH to "如何在应用内直接给住宿方拨打电话？",
            LanguageMode.NL to "Hoe bel je een accommodatie rechtstreeks vanuit de app?",
            LanguageMode.DE to "Wie ruft man eine Unterkunft direkt aus der App an?",
            LanguageMode.PL to "Jak zadzwonić do noclegu bezpośrednio z aplikacji?"
        ),
        answers = mapOf(
            LanguageMode.ES to "Al guardar el número de teléfono del alojamiento, aparecerá un botón 'Llamar' que abrirá directamente el marcador telefónico de tu dispositivo.",
            LanguageMode.EN to "When you save the property phone number, a 'Call' button appears, launching your device dialer with the number pre-filled.",
            LanguageMode.FR to "Lorsque le numéro de téléphone est enregistré, un bouton 'Appeler' ouvre directement votre clavier d'appel avec le numéro prérempli.",
            LanguageMode.IT to "Salvando il numero di telefono dell'alloggio, apparirà il pulsante 'Chiama' per avviare subito la chiamata dal tastierino telefonico.",
            LanguageMode.ZH to "在住宿卡片中输入电话号码保存后，点击出现的“拨打电话”按钮即可一键调出手机拨号盘，省去复制号码的操作。",
            LanguageMode.NL to "Als je het telefoonnummer opslaat, verschijnt een 'Bellen'-knop die direct je telefoontoetsenbord opent met het nummer ingevuld.",
            LanguageMode.DE to "Sobald eine Telefonnummer hinterlegt ist, erscheint die Taste 'Anrufen', die die Wähltastatur des Telefons direkt öffnet.",
            LanguageMode.PL to "Po zapisaniu numeru telefonu pojawi się przycisk 'Zadzwoń', który od razu uruchomi połączenie w telefonie."
        )
    ),
    FaqItem(
        id = "faq_booking_budget",
        categoryKey = "accommodations",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo controlar el estado de reserva y el gasto acumulado?",
            LanguageMode.EN to "How to track booking status and cumulative spending?",
            LanguageMode.FR to "Comment suivre l'état des réservations et les dépenses cumulées ?",
            LanguageMode.IT to "Come monitorare le prenotazioni e la spesa totale degli alloggi?",
            LanguageMode.ZH to "如何跟踪预订状态以及住宿累计支出？",
            LanguageMode.NL to "Hoe houd je boekingen en totale verblijfskosten bij?",
            LanguageMode.DE to "Wie behält man Buchungen und Gesamtausgaben im Blick?",
            LanguageMode.PL to "Jak kontrolować status rezerwacji i łączne wydatki na noclegi?"
        ),
        answers = mapOf(
            LanguageMode.ES to "Marca la casilla 'Reserva confirmada'. La aplicación sumará automáticamente el importe en el 'Balance General' de la pestaña Datos.",
            LanguageMode.EN to "Toggle the 'Booking Confirmed' checkbox. The app automatically tallies accommodation expenses in the 'General Balance' within the Data tab.",
            LanguageMode.FR to "Cochez 'Réservation confirmée'. Le montant est automatiquement comptabilisé dans le 'Bilan Général' de l'onglet Données.",
            LanguageMode.IT to "Attiva la casella 'Prenotazione confermata'. L'importo verrà conteggiato automaticamente nel 'Bilancio Generale' della scheda Dati.",
            LanguageMode.ZH to "勾选“已预订确认”，系统将自动把相应金额累计到“数据”标签页的“总体统计”中，方便掌控总开支。",
            LanguageMode.NL to "Vink 'Reservering bevestigd' aan. De app telt de kosten automatisch op in de 'Algemene Balans' op het tabblad Gegevens.",
            LanguageMode.DE to "Aktivieren Sie 'Buchung bestätigt'. Die App rechnet die Beträge automatisch in der 'Gesamtbilanz' im Tab Daten zusammen.",
            LanguageMode.PL to "Zaznacz opcję 'Rezerwacja potwierdzona'. Kwota zostanie automatycznie zliczona w 'Bilansie Ogólnym' w zakładce Dane."
        )
    ),

    // 4. Vivencias y Fotos
    FaqItem(
        id = "faq_journal_memories",
        categoryKey = "journal",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo escribir mi diario de vivencias y reflexiones?",
            LanguageMode.EN to "How to write my journal of daily reflections and memories?",
            LanguageMode.FR to "Comment rédiger mon carnet de souvenirs quotidiens ?",
            LanguageMode.IT to "Come scrivere il diario delle memorie ed esperienze giornaliere?",
            LanguageMode.ZH to "如何撰写每日回忆与旅行心得日记？",
            LanguageMode.NL to "Hoe schrijf je een reisdagboek met dagelijkse ervaringen?",
            LanguageMode.DE to "Wie schreibt man das Tagebuch der täglichen Erlebnisse?",
            LanguageMode.PL to "Jak prowadzić dziennik codziennych wspomnień i refleksji?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En cada jornada encontrarás el apartado 'Vivencias Diarias'. Escribe tus experiencias y pulsa 'Guardar Notas' para conservarlas e incluirlas en tu PDF.",
            LanguageMode.EN to "In each stage you will find the 'Daily Memories' section. Write down your personal reflections and tap 'Save Journal' to keep them and export to PDF.",
            LanguageMode.FR to "Dans chaque étape, utilisez la section 'Souvenirs Quotidiens'. Écrivez vos notes et cliquez sur 'Enregistrer' pour les inclure dans le PDF.",
            LanguageMode.IT to "In ogni tappa trovi la sezione 'Ricordi Giornalieri'. Scrivi le tue note e tocca 'Salva Diario' per includerle anche nel PDF esportato.",
            LanguageMode.ZH to "在每日详情中找到“每日回忆与日记”区域，记录所见所闻与心灵感悟，点击“保存日记”即可永久存储并可一并导出至 PDF 旅行指南中。",
            LanguageMode.NL to "Onder elke etappe vind je 'Dagelijkse Herinneringen'. Schrijf je ervaringen op en klik op 'Opslaan' om ze ook in je PDF op te nemen.",
            LanguageMode.DE to "In jeder Etappe gibt es den Bereich 'Tägliche Erlebnisse'. Notieren Sie Ihre Eindrücke und tippen Sie auf 'Speichern', um sie ins PDF einzubinden.",
            LanguageMode.PL to "W każdym dniu znajdziesz sekcję 'Codzienne Wspomnienia'. Zapisz swoje przemyślenia i kliknij 'Zapisz Dziennik', aby zachować je w przewodniku PDF."
        )
    ),
    FaqItem(
        id = "faq_photos",
        categoryKey = "journal",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo adjuntar fotos a una etapa o día?",
            LanguageMode.EN to "How to attach photos to a stage or travel day?",
            LanguageMode.FR to "Comment joindre des photos à une étape ou journée ?",
            LanguageMode.IT to "Come allegare fotografie a una tappa o giorno?",
            LanguageMode.ZH to "如何为赛段或日程添加照片？",
            LanguageMode.NL to "Hoe voeg je foto's toe aan een etappe of reisdag?",
            LanguageMode.DE to "Wie hängt man Fotos an eine Etappe oder einen Reisetag an?",
            LanguageMode.PL to "Jak dołączyć zdjęcia do etapu lub dnia?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En el bloque de vivencias pulsa 'Adjuntar Foto' para seleccionar cualquier imagen de la galería de tu teléfono móvil.",
            LanguageMode.EN to "In the journal block, tap 'Attach Photo' to choose pictures from your device gallery.",
            LanguageMode.FR to "Dans la section souvenirs, appuyez sur 'Joindre une Photo' pour choisir des images dans votre galerie.",
            LanguageMode.IT to "Nel riquadro dei ricordi, tocca 'Allega Foto' per selezionare immagini dalla galleria dello smartphone.",
            LanguageMode.ZH to "在日记卡片中点击“添加照片”，即可从手机本地相册中选取具有代表性的美景照片附加到该日程中。",
            LanguageMode.NL to "Tik in het dagboek op 'Foto Toevoegen' om afbeeldingen uit de galerij van je telefoon te selecteren.",
            LanguageMode.DE to "Tippen Sie im Tagebuchbereich auf 'Foto Anhängen', um Bilder aus Ihrer Telefongalerie auszuwählen.",
            LanguageMode.PL to "W sekcji wspomnień wybierz 'Dołącz Zdjęcie', aby wybrać ulubione kadry z galerii swojego telefonu."
        )
    ),

    // 5. Viajes y Agenda
    FaqItem(
        id = "faq_agenda",
        categoryKey = "agenda",
        questions = mapOf(
            LanguageMode.ES to "¿Qué es la Agenda de Viajes y cómo adjuntar billetes o reservas?",
            LanguageMode.EN to "What is the Trips Agenda and how to attach tickets or vouchers?",
            LanguageMode.FR to "Qu'est-ce que l'Agenda de Voyage et comment joindre des billets ?",
            LanguageMode.IT to "Cos'è l'Agenda di Viaggio e come allegare biglietti o voucher?",
            LanguageMode.ZH to "什么是旅行日程表，如何附带车票、机票或门票凭据？",
            LanguageMode.NL to "Wat is de Reisagenda en hoe voeg je tickets en vouchers toe?",
            LanguageMode.DE to "Was ist das Reiseprogramm und wie hängt man Tickets an?",
            LanguageMode.PL to "Czym jest Harmonogram Wyprawy i jak dołączyć bilety lub rezerwacje?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En el perfil de Viajes, cada día incluye 'Agenda y Actividades'. Permite fijar horarios de visitas y adjuntar billetes o reservas en PDF e imagen sin conexión.",
            LanguageMode.EN to "In the Trips profile, each day includes 'Schedule & Activities'. You can set timed visits and attach PDF or image tickets accessible offline.",
            LanguageMode.FR to "Dans le profil Voyages, la section 'Agenda' permet de programmer des horaires et de stocker des billets PDF ou photos hors-ligne.",
            LanguageMode.IT to "Nel profilo Viaggi, 'Agenda e Attività' permette di pianificare orari e allegare biglietti PDF o immagini consultabili offline.",
            LanguageMode.ZH to "在旅行模式下，每天均设有“日程与活动”版块，可精确定位各景点的游览时间，并附带电子客票、酒店预订单的 PDF 或图片，离线随时调取。",
            LanguageMode.NL to "In het Reizen-profiel biedt elke dag een 'Agenda'. Plan bezoeken en bewaar offline beschikbare pdf-tickets of afbeeldingsvouchers.",
            LanguageMode.DE to "Im Reisen-Profil bietet jeder Tag ein 'Programm'. Planen Sie Besichtigungszeiten und hängen Sie PDF-Tickets oder Vouchers offline an.",
            LanguageMode.PL to "W profilu Wypraw każdy dzień ma sekcję 'Harmonogram'. Umożliwia planowanie godzin zwiedzania i dołączanie biletów PDF/zdjęć offline."
        )
    ),

    // 6. Estadísticas y Balances
    FaqItem(
        id = "faq_stats",
        categoryKey = "stats",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo se calculan las estadísticas y los balances generales?",
            LanguageMode.EN to "How are statistics and general balances calculated?",
            LanguageMode.FR to "Comment les statistiques et bilans généraux sont-ils calculés ?",
            LanguageMode.IT to "Come vengono calcolate le statistiche e i bilanci generali?",
            LanguageMode.ZH to "各项统计数据与总体平衡表是如何计算的？",
            LanguageMode.NL to "Hoe worden statistieken en algemene balansen berekend?",
            LanguageMode.DE to "Wie werden Statistiken und Gesamtbilanzen berechnet?",
            LanguageMode.PL to "Jak obliczane są statystyki i bilanse ogólne?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En 'Datos' dispones de dos balances independientes. La app separa estrictamente Caminos y Viajes: las rutas de Caminos solo suman a Caminos, y los Viajes a Viajes.",
            LanguageMode.EN to "In 'Data' you have two independent views. Calculations follow strict bimodal separation: Camino routes only sum into Caminos, and Trips only into Trips.",
            LanguageMode.FR to "L'onglet 'Données' sépare strictement les Chemins et les Voyages : les totaux de chacun sont calculés de manière totalement indépendante.",
            LanguageMode.IT to "Nella scheda 'Dati' i conteggi sono separati: i percorsi dei Cammini sommano solo nei Cammini, e i Viaggi solo nei Viaggi.",
            LanguageMode.ZH to "在“数据”面板中提供两套独立平衡表。系统严格实行双模分离：朝圣之路的数据仅计入朝圣汇总，旅行路线仅计入旅行汇总，绝不混淆。",
            LanguageMode.NL to "Onder 'Gegevens' zijn berekeningen strikt gescheiden: Camino's tellen alleen mee voor Camino's, en Reizen alleen voor Reizen.",
            LanguageMode.DE to "Im Tab 'Daten' gilt eine strikte Trennung: Wege fließen ausschließlich in die Wege-Bilanz ein, Reisen nur in die Reisen-Bilanz.",
            LanguageMode.PL to "W zakładce 'Dane' obliczenia są rozdzielone: trasy Szlaków wliczają się tylko do Szlaków, a Wyprawy tylko do Wypraw."
        )
    ),

    // 7. Recompensas y Logros
    FaqItem(
        id = "faq_rewards",
        categoryKey = "rewards",
        questions = mapOf(
            LanguageMode.ES to "¿Qué reconocimientos se obtienen al completar el 100%?",
            LanguageMode.EN to "What rewards do you earn upon completing 100% of routes?",
            LanguageMode.FR to "Quelles récompenses obtient-on en complétant 100 % d'un parcours ?",
            LanguageMode.IT to "Quali riconoscimenti si ottengono completando il 100%?",
            LanguageMode.ZH to "当完成 100% 的路线时会获得何种荣誉奖励？",
            LanguageMode.NL to "Welke beloningen ontgrendel je bij het voltooien van 100%?",
            LanguageMode.DE to "Welche Auszeichnungen erhält man beim Erreichen von 100 %?",
            LanguageMode.PL to "Jakie odznaki otrzymuje się po ukończeniu 100% tras?"
        ),
        answers = mapOf(
            LanguageMode.ES to "Al completar el 100%, la app desbloquea un distintivo adaptado al perfil activo: en Caminos obtendrás la tradicional 'Compostela', y en Viajes el 'Pasaporte de Viajero'.",
            LanguageMode.EN to "Upon 100% completion, the app awards a profile-tailored badge: the traditional 'Compostela' in Caminos, and the 'Traveler Passport' in Trips.",
            LanguageMode.FR to "À 100 %, l'application débloque un badge adapté : la traditionnelle 'Compostela' pour les Chemins, ou le 'Passeport de Voyageur' pour les Voyages.",
            LanguageMode.IT to "Al completamento del 100%, si sblocca un riconoscimento su misura: nei Cammini la 'Compostela', e nei Viaggi il 'Passaporto del Viaggiatore'.",
            LanguageMode.ZH to "达成 100% 完赛后，系统将解锁专属纪念认证：朝圣之路模式颁发传统的“孔波斯特拉证书”，旅行模式则颁发“旅行者护照勋章”。",
            LanguageMode.NL to "Bij 100% voltooiing ontgrendel je een speciaal ereteken: de 'Compostela' bij Camino's en het 'Reispaspoort' bij Reizen.",
            LanguageMode.DE to "Bei 100 % schaltet die App eine profilbezogene Auszeichnung frei: die traditionelle 'Compostela' bei Wegen und den 'Reisepass' bei Touren.",
            LanguageMode.PL to "Po ukończeniu 100% aplikacja przyznaje nagrodę: tradycyjną 'Compostelę' na Szlakach oraz 'Paszport Podróżnika' w profilu Wypraw."
        )
    ),

    // 8. Exportación y PDF
    FaqItem(
        id = "faq_pdf",
        categoryKey = "pdf",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo exportar una guía completa en PDF lista para imprimir?",
            LanguageMode.EN to "How to export a complete travel guide in PDF ready to print?",
            LanguageMode.FR to "Comment exporter un guide complet en PDF prêt à imprimer ?",
            LanguageMode.IT to "Come esportare una guida completa in PDF pronta da stampare?",
            LanguageMode.ZH to "如何导出格式精美、适合打印的完整 PDF 旅行指南？",
            LanguageMode.NL to "Hoe exporteer je een complete reisgids in printklare pdf?",
            LanguageMode.DE to "Wie exportiert man einen druckfertigen Reiseführer als PDF?",
            LanguageMode.PL to "Jak wyeksportować kompletny przewodnik PDF gotowy do druku?"
        ),
        answers = mapOf(
            LanguageMode.ES to "Desde el menú de opciones (tres puntos) o en 'Ajustes' -> 'Exportar PDF', genera un documento con portada, índice, mapas cenitales 2D, consejos y alojamientos.",
            LanguageMode.EN to "From the options menu (three dots) or 'Settings' -> 'Export PDF', generate a guide with custom cover, table of contents, 2D maps, tips, and accommodations.",
            LanguageMode.FR to "Depuis le menu d'options ou 'Paramètres' -> 'Exporter PDF', générez un guide élégant avec couverture, cartes 2D, conseils et hébergements.",
            LanguageMode.IT to "Dal menu opzioni o da 'Impostazioni' -> 'Esporta PDF', genera una guida con copertina, mappe 2D zenitali, consigli e schede alloggi.",
            LanguageMode.ZH to "在右上角选项菜单（三个点）或“设置”->“导出 PDF”中生成高品质文档，内含专属封面、赛段目录、2D 俯视地图、每日贴士及住宿清单。",
            LanguageMode.NL to "Genereer via het optiemenu of 'Instellingen' -> 'Exporteer PDF' een document met omslag, 2D-kaarten, dagtips en accommodatielijsten.",
            LanguageMode.DE to "Erstellen Sie über das Dreipunkt-Menü oder 'Einstellungen' -> 'PDF Exportieren' ein Dokument mit Titelseite, 2D-Karten, Tipps und Unterkünften.",
            LanguageMode.PL to "Z menu opcji lub w 'Ustawieniach' -> 'Eksportuj PDF' wygeneruj dokument z okładką, spisem treści, mapami 2D, wskazówkami i noclegami."
        )
    ),
    FaqItem(
        id = "faq_pdf_2d_maps",
        categoryKey = "pdf",
        questions = mapOf(
            LanguageMode.ES to "¿Por qué los mapas del PDF se imprimen en vista cenital plana 2D?",
            LanguageMode.EN to "Why are PDF maps rendered in 2D flat top-down view?",
            LanguageMode.FR to "Pourquoi les cartes du PDF sont-elles imprimées en vue 2D zénithale ?",
            LanguageMode.IT to "Perché le mappe del PDF sono stampate in vista zenitale 2D?",
            LanguageMode.ZH to "为什么 PDF 中的地图采用 2D 平面鸟瞰视角呈现？",
            LanguageMode.NL to "Waarom worden pdf-kaarten in een platte 2D-bovenaanzicht afgedrukt?",
            LanguageMode.DE to "Warum werden PDF-Karten in flacher 2D-Draufsicht gedruckt?",
            LanguageMode.PL to "Dlaczego mapy w pliku PDF są drukowane w płaskim widoku 2D z góry?"
        ),
        answers = mapOf(
            LanguageMode.ES to "La vista cenital 2D con Google Roadmap garantiza máxima legibilidad en papel impreso, evita deformaciones de perspectiva 3D y ahorra tinta de impresión.",
            LanguageMode.EN to "The 2D top-down perspective with Google Roadmap ensures clear legibility on printed paper, eliminates 3D distortions, and saves printer ink.",
            LanguageMode.FR to "La vue 2D zénithale avec Google Roadmap garantit une clarté optimale sur papier, sans distorsion 3D, tout en économisant l'encre.",
            LanguageMode.IT to "La vista piana 2D garantisce la massima leggibilità sulla carta stampata, senza distorsioni prospettiche 3D e riducendo l'uso d'inchiostro.",
            LanguageMode.ZH to "采用 Google Roadmap 的 2D 俯视平面图专为纸质打印深度优化，地名路标清晰锐利，无 3D 视角透视畸变，且大幅节省打印机油墨耗材。",
            LanguageMode.NL to "Het 2D-bovenaanzicht zorgt voor maximale leesbaarheid op papier, voorkomt 3D-vervorming en bespaart printinkt.",
            LanguageMode.DE to "Die flache 2D-Draufsicht sorgt für optimale Lesbarkeit auf Papier, verhindert 3D-Verzerrungen und spart Druckertinte.",
            LanguageMode.PL to "Płaski widok 2D z góry gwarantuje doskonałą czytelność na papierze, eliminuje zniekształcenia perspektywy 3D i oszczędza tusz w drukarce."
        )
    ),

    // 9. Copias de Seguridad y Datos
    FaqItem(
        id = "faq_backup",
        categoryKey = "backup",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo funciona la Copia de Seguridad y Restauración?",
            LanguageMode.EN to "How does Backup and Data Restoration work?",
            LanguageMode.FR to "Comment fonctionnent la sauvegarde et la restauration des données ?",
            LanguageMode.IT to "Come funzionano il backup e il ripristino dei dati?",
            LanguageMode.ZH to "如何进行完整数据备份与恢复？",
            LanguageMode.NL to "Hoe werken back-up en herstel van gegevens?",
            LanguageMode.DE to "Wie funktionieren Datensicherung und Wiederherstellung?",
            LanguageMode.PL to "Jak działa tworzenie kopii zapasowej i przywracanie danych?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En 'Ajustes' -> 'Copia de Seguridad' puedes exportar copias completas o individuales en formato ZIP o JSON. Al restaurar, tus rutas y recuerdos se reincorporan con seguridad.",
            LanguageMode.EN to "In 'Settings' -> 'Backup and Restore', you can export full or individual backups as ZIP or JSON. Restoring safely recovers your routes and memories.",
            LanguageMode.FR to "Dans 'Paramètres' -> 'Sauvegarde', exportez des copies ZIP ou JSON. La restauration réintègre vos parcours en toute sécurité.",
            LanguageMode.IT to "In 'Impostazioni' -> 'Backup', puoi esportare archivi completi o singoli in formato ZIP o JSON e ripristinarli quando vuoi in totale sicurezza.",
            LanguageMode.ZH to "在“设置”->“备份与恢复”中，可导出 ZIP 或 JSON 格式的完整全局备份或单条路线备份；导入时数据将安全无缝还原。",
            LanguageMode.NL to "Onder 'Instellingen' -> 'Back-up' exporteer je volledige of afzonderlijke back-ups als zip of json en herstel je ze veilig.",
            LanguageMode.DE to "Unter 'Einstellungen' -> 'Backup' können Sie Archive im ZIP- oder JSON-Format exportieren und sicher wiederherstellen.",
            LanguageMode.PL to "W 'Ustawieniach' -> 'Kopia Zapasowa' wyeksportujesz pliki ZIP lub JSON, a funkcja przywracania bezpiecznie odzyska Twoje dane."
        )
    ),
    FaqItem(
        id = "faq_backup_duplicates",
        categoryKey = "backup",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo gestiona la app los nombres duplicados al importar?",
            LanguageMode.EN to "How does the app handle duplicate route names during import?",
            LanguageMode.FR to "Comment l'application gère-t-elle les doublons lors de l'importation ?",
            LanguageMode.IT to "Come gestisce l'app i nomi duplicati durante l'importazione?",
            LanguageMode.ZH to "导入备份时如果遇到重名路线，应用如何处理？",
            LanguageMode.NL to "Hoe gaat de app om met dubbele routenamen bij het importeren?",
            LanguageMode.DE to "Wie behandelt die App doppelte Routennamen beim Import?",
            LanguageMode.PL to "Jak aplikacja radzi sobie ze zduplikowanymi nazwami podczas importu?"
        ),
        answers = mapOf(
            LanguageMode.ES to "La app cuenta con normalización inteligente: ignora acentos y mayúsculas (ej. 'Baztán' y 'Baztan'), detectando duplicados y preguntándote si deseas sobreescribir.",
            LanguageMode.EN to "The app features smart normalization: it ignores accents and casing (e.g. 'Baztán' and 'Baztan'), detecting duplicates and asking whether to overwrite.",
            LanguageMode.FR to "Grâce à une normalisation intelligente qui ignore accents et majuscules, l'application détecte les doublons et vous demande s'il faut les écraser.",
            LanguageMode.IT to "L'app normalizza i nomi ignorando accenti e maiuscole, rilevando coincidenze (es. 'Baztán' e 'Baztan') e chiedendoti se sovrascrivere.",
            LanguageMode.ZH to "应用具备智能归一化比对算法：自动忽略重音符号与大小写（如“Baztán”与“Baztan”视为相同），并在冲突时友好询问是否覆盖。",
            LanguageMode.NL to "De app negeert accenten en hoofdletters (bijv. 'Baztán' en 'Baztan'), herkent duplicaten en vraagt of je ze wilt overschrijven.",
            LanguageMode.DE to "Die App ignoriert Akzente und Groß-/Kleinschreibung beim Abgleich und fragt bei Duplikaten nach, ob überschrieben werden soll.",
            LanguageMode.PL to "Aplikacja inteligentnie ignoruje akcenty i wielkość liter (np. 'Baztán' i 'Baztan'), pytając, czy chcesz nadpisać istniejącą trasę."
        )
    ),

    // 10. Perfiles y Portada
    FaqItem(
        id = "faq_profiles",
        categoryKey = "profiles",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo cambiar entre el perfil de Caminos y el perfil de Viajes?",
            LanguageMode.EN to "How to switch between Caminos and Travel profiles?",
            LanguageMode.FR to "Comment basculer entre le profil Chemins et le profil Voyages ?",
            LanguageMode.IT to "Come passare dal profilo Cammini a quello Viaggi?",
            LanguageMode.ZH to "如何在朝圣之路与徒步旅行两种档案模式之间切换？",
            LanguageMode.NL to "Hoe wissel je tussen het Camino- en het Reizen-profiel?",
            LanguageMode.DE to "Wie wechselt man zwischen dem Wege- und dem Reisen-Profil?",
            LanguageMode.PL to "Jak przełączać się między profilem Szlaków a Wypraw?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En la cabecera superior pulsa el icono de Caminante o Maleta. La app adaptará colores, textos ('Etapas' vs 'Días') y recordará la última ruta abierta en cada perfil.",
            LanguageMode.EN to "In the top header, tap the Hiker or Suitcase icon. The app adjusts colors, wording ('Stages' vs 'Days'), and remembers the last active route per profile.",
            LanguageMode.FR to "En haut de l'écran, touchez l'icône Randonneur ou Valise pour adapter les couleurs, les textes et mémoriser le dernier itinéraire ouvert.",
            LanguageMode.IT to "Nell'intestazione tocca l'icona del Viandante o della Valigia. L'app adatta i colori, i testi e ricorda l'ultimo itinerario aperto per ciascun profilo.",
            LanguageMode.ZH to "在顶部工具栏点击“徒步者”或“旅行箱”图标。系统会自动切换色系、专属用语（“赛段”对比“日程”），并分别记忆各模式下最后打开的路线。",
            LanguageMode.NL to "Tik in de bovenbalk op het wandelaar- of kofferpictogram. De app past kleuren en termen aan en onthoudt per profiel de laatst geopende route.",
            LanguageMode.DE to "Tippen Sie oben auf das Wanderer- oder Koffersymbol. Die App passt Farbgebung und Begriffe an und merkt sich die zuletzt geöffnete Route je Profil.",
            LanguageMode.PL to "W górnym nagłówku naciśnij ikonę Pielgrzyma lub Walizki. Aplikacja dostosuje kolory, nazwy oraz zapamięta ostatnio otwartą trasę w każdym profilu."
        )
    ),
    FaqItem(
        id = "faq_cover_photo",
        categoryKey = "profiles",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo personalizar la foto de portada de un camino o viaje?",
            LanguageMode.EN to "How to customize a route cover photo?",
            LanguageMode.FR to "Comment personnaliser la photo de couverture d'un itinéraire ?",
            LanguageMode.IT to "Come personalizzare la foto di copertina di un percorso?",
            LanguageMode.ZH to "如何自定义朝圣之路或旅行路线的封面主图？",
            LanguageMode.NL to "Hoe pas je de omslagfoto van een route aan?",
            LanguageMode.DE to "Wie passt man das Titelbild einer Route an?",
            LanguageMode.PL to "Jak zmienić zdjęcie na okładce szlaku lub wyprawy?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En la cabecera de la ruta pulsa el botón de fotografía para elegir una imagen de tu galería. Se mostrará en la cabecera de la app y en la portada del PDF.",
            LanguageMode.EN to "In the route header, tap the photo icon button to select any image from your gallery. It displays on the banner and becomes the PDF guide front cover.",
            LanguageMode.FR to "Touchez le bouton photo sur la bannière pour sélectionner une image de votre galerie, visible sur l'application et la couverture du PDF.",
            LanguageMode.IT to "Nel banner del percorso tocca il pulsante foto per scegliere un'immagine dalla galleria: apparirà nell'app e sulla copertina del PDF.",
            LanguageMode.ZH to "在路线顶部卡片右下角点击“相机照片”按钮，从相册中选择喜爱的相片，即可作为应用横幅展示图及 PDF 导出指南的头版大图。",
            LanguageMode.NL to "Tik in de banner op het fotopictogram om een afbeelding uit je galerij te kiezen. Deze siert de app en de voorpagina van je pdf.",
            LanguageMode.DE to "Tippen Sie auf das Fotosymbol im Banner, um ein Galeriebild auszuwählen. Es wird im App-Kopf und auf dem PDF-Cover angezeigt.",
            LanguageMode.PL to "W nagłówku trasy naciśnij przycisk aparatu, aby wybrać zdjęcie z galerii. Pojawi się ono na banerze oraz na okładce pliku PDF."
        )
    ),

    // 11. Ajustes y Consejos
    FaqItem(
        id = "faq_settings_theme_lang",
        categoryKey = "settings",
        questions = mapOf(
            LanguageMode.ES to "¿Cómo cambiar el idioma y el tema visual (Modo Oscuro / Claro)?",
            LanguageMode.EN to "How to change language and display theme (Dark / Light Mode)?",
            LanguageMode.FR to "Comment changer la langue et le thème (Mode Sombre / Clair) ?",
            LanguageMode.IT to "Come cambiare lingua e tema visivo (Chiaro / Scuro)?",
            LanguageMode.ZH to "如何切换应用显示语言与深色/浅色视觉主题？",
            LanguageMode.NL to "Hoe verander je de taal en het visuele thema (Donker / Licht)?",
            LanguageMode.DE to "Wie ändert man Sprache und Design (Dunkel- / Hellmodus)?",
            LanguageMode.PL to "Jak zmienić język i motyw wizualny (Ciemny / Jasny)?"
        ),
        answers = mapOf(
            LanguageMode.ES to "En 'Ajustes' puedes alternar entre los 8 idiomas soportados y elegir entre Modo Claro, Oscuro o Sistema con efecto inmediato.",
            LanguageMode.EN to "In 'Settings' you can switch among 8 supported languages and choose Light, Dark, or System theme with instant effect.",
            LanguageMode.FR to "Dans 'Paramètres', basculez entre 8 langues et choisissez le mode Sombre, Clair ou Système instantanément.",
            LanguageMode.IT to "In 'Impostazioni' puoi scegliere tra 8 lingue supportate e impostare la modalità Chiara, Scura o Sistema con effetto immediato.",
            LanguageMode.ZH to "在“设置”中可在 8 种支持的语言之间实时切换，并可在深色模式、浅色模式或跟随系统间自由设定，无需重启应用。",
            LanguageMode.NL to "Onder 'Instellingen' wissel je direct tussen 8 ondersteunde talen en kies je voor het lichte, donkere of systeemthema.",
            LanguageMode.DE to "Unter 'Einstellungen' können Sie sofort zwischen 8 Sprachen wechseln und das helle, dunkle oder System-Design aktivieren.",
            LanguageMode.PL to "W 'Ustawieniach' możesz natychmiast przełączać między 8 językami oraz wybrać tryb Jasny, Ciemny lub Systemowy."
        )
    ),
    FaqItem(
        id = "faq_stage_tips",
        categoryKey = "settings",
        questions = mapOf(
            LanguageMode.ES to "¿Qué son los Consejos del Día y cómo editarlos?",
            LanguageMode.EN to "What are Tips of the Day and how to edit them?",
            LanguageMode.FR to "Que sont les Conseils du Jour et comment les modifier ?",
            LanguageMode.IT to "Cosa sono i Consigli del Giorno e come modificarli?",
            LanguageMode.ZH to "什么是每日贴士，如何对其进行编辑？",
            LanguageMode.NL to "Wat zijn de Tips van de Dag en hoe bewerk je ze?",
            LanguageMode.DE to "Was sind die Tipps des Tages und wie bearbeitet man sie?",
            LanguageMode.PL to "Czym są Wskazówki Dnia i jak je edytować?"
        ),
        answers = mapOf(
            LanguageMode.ES to "Cada etapa incluye recomendaciones sobre agua, calzado o variantes de ruta. Pulsa 'Editar Consejos' para personalizar las notas.",
            LanguageMode.EN to "Each stage includes recommendations on water sources, gear, and trails. Tap 'Edit Tips' to customize your notes.",
            LanguageMode.FR to "Chaque étape offre des conseils sur les points d'eau et sentiers. Appuyez sur 'Modifier les Conseils' pour ajouter vos notes.",
            LanguageMode.IT to "Ogni tappa contiene suggerimenti su fonti d'acqua, calzature e sentieri. Tocca 'Modifica Consigli' per personalizzarli.",
            LanguageMode.ZH to "每个赛段均内置补给点、饮用水、鞋袜装备及历史古道分支提示，点击“编辑贴士”即可随时写入您自己的专属经验与提醒。",
            LanguageMode.NL to "Elke etappe bevat advies over waterpunten, schoeisel en wandelpaden. Tik op 'Tips Bewerken' om ze aan te passen.",
            LanguageMode.DE to "Jede Etappe bietet Empfehlungen zu Wasserquellen, Schuhwerk und Wegen. Tippen Sie auf 'Tipps Bearbeiten' für eigene Notizen.",
            LanguageMode.PL to "Każdy etap zawiera porady dotyczące wody, obuwia i szlaku. Dotknij 'Edytuj Wskazówki', aby dodać własne notatki."
        )
    )
)

private fun normalizeSearchText(text: String): String {
    return BackupManager.stripAccents(text.trim())
        .lowercase()
        .replace("[^\\p{L}\\p{Nd}\\s]".toRegex(), " ")
        .replace("\\s+".toRegex(), " ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    viewModel: CaminoViewModel,
    modifier: Modifier = Modifier
) {
    val languageMode by viewModel.languageMode.collectAsStateWithLifecycle()

    fun tr(key: String): String = viewModel.string(key, languageMode)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryKey by remember { mutableStateOf<String?>(null) }
    var expandedFaqIds by remember { mutableStateOf(setOf<String>()) }

    // Filtrado en tiempo real con normalización internacional (soporta caracteres latinos, acentos, diéresis y chino)
    val filteredFaqs = remember(searchQuery, selectedCategoryKey, languageMode) {
        val normQuery = normalizeSearchText(searchQuery)
        val queryTerms = if (normQuery.isBlank()) emptyList() else normQuery.split(" ").filter { it.isNotBlank() }

        INITIAL_FAQS.filter { item ->
            val matchesCategory = selectedCategoryKey == null || item.categoryKey == selectedCategoryKey
            if (!matchesCategory) return@filter false
            if (queryTerms.isEmpty()) return@filter true

            val currentQuestion = normalizeSearchText(item.question(languageMode))
            val currentAnswer = normalizeSearchText(item.answer(languageMode))
            val currentCategory = normalizeSearchText(
                FAQ_CATEGORIES.firstOrNull { it.key == item.categoryKey }?.label(languageMode) ?: ""
            )

            // Indexar también todas las traducciones para búsqueda universal
            val allQuestions = item.questions.values.joinToString(" ") { normalizeSearchText(it) }
            val allAnswers = item.answers.values.joinToString(" ") { normalizeSearchText(it) }

            val searchableCorpus = "$currentQuestion $currentAnswer $currentCategory $allQuestions $allAnswers"

            queryTerms.all { term -> searchableCorpus.contains(term) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = tr("help_title"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CaminoBlueDark
                        )
                        Text(
                            text = tr("help_subtitle"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("help_top_app_bar")
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // 1. Buscador Inteligente en tiempo real
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("help_search_field"),
                placeholder = {
                    Text(
                        text = tr("search_faq_placeholder"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = tr("search"),
                        tint = CaminoBlue
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("btn_clear_search")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = tr("faq_clear_search"),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CaminoBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(Modifier.height(12.dp))

            // 2. Chips de Categorías reactivos a los 8 idiomas
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Chip 0: Todas / All
                item {
                    val isAllSelected = selectedCategoryKey == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { selectedCategoryKey = null },
                        label = {
                            Text(
                                text = tr("faq_category_all"),
                                fontSize = 12.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PilgrimYellow,
                            selectedLabelColor = CaminoBlueDark,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            0.8.dp,
                            if (isAllSelected) PilgrimYellow else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                items(FAQ_CATEGORIES, key = { it.key }) { cat ->
                    val isSelected = selectedCategoryKey == cat.key
                    val label = cat.label(languageMode)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategoryKey = if (isSelected) null else cat.key
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PilgrimYellow,
                            selectedLabelColor = CaminoBlueDark,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            0.8.dp,
                            if (isSelected) PilgrimYellow else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // 3. Lista de Preguntas Frecuentes (Acordeón expandible)
            if (filteredFaqs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PilgrimYellowLight,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = CaminoBlueDark,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "${tr("faq_no_results")} \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = tr("faq_no_results_hint"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategoryKey = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CaminoBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = tr("faq_clear_search"),
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("help_faq_list"),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredFaqs, key = { it.id }) { item ->
                        val isExpanded = expandedFaqIds.contains(item.id)
                        val rotationState by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            label = "chevron_rotation"
                        )
                        val question = item.question(languageMode)
                        val answer = item.answer(languageMode)
                        val category = FAQ_CATEGORIES.firstOrNull { it.key == item.categoryKey }?.label(languageMode) ?: ""

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isExpanded) CaminoBlue.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable {
                                    expandedFaqIds = if (isExpanded) {
                                        expandedFaqIds - item.id
                                    } else {
                                        expandedFaqIds + item.id
                                    }
                                }
                                .testTag("faq_item_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = PilgrimYellowLight,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CaminoBlueDark,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) tr("collapse") else tr("expand"),
                                        tint = CaminoBlue,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .rotate(rotationState)
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = question,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )

                                AnimatedVisibility(visible = isExpanded) {
                                    Column {
                                        Spacer(Modifier.height(10.dp))
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                            thickness = 0.8.dp
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = answer,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 21.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
