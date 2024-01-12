#pragma once

#include <any>
#include <string>

#include "benchmark.h"
#include "som/error.h"
#include "som/vector.h"

using std::string;

const string rapBenchmarkMinified =  // NOLINT
    "{\"head\":{\"requestCounter\":4},\"operations\":[[\"destroy\",\"w54\"],["
    "\"set\",\"w2\",{\"activeControl\":\"w99\"}],[\"set\",\"w21\",{"
    "\"customVariant\":\"variant_navigation\"}],[\"set\",\"w28\",{"
    "\"customVariant\":\"variant_selected\"}],[\"set\",\"w53\",{\"children\":["
    "\"w95\"]}],[\"create\",\"w95\",\"rwt.widgets.Composite\",{\"parent\":"
    "\"w53\",\"style\":[\"NONE\"],\"bounds\":[0,0,1008,586],\"children\":["
    "\"w96\",\"w97\"],\"tabIndex\":-1,\"clientArea\":[0,0,1008,586]}],["
    "\"create\",\"w96\",\"rwt.widgets.Label\",{\"parent\":\"w95\",\"style\":["
    "\"NONE\"],\"bounds\":[10,30,112,26],\"tabIndex\":-1,\"customVariant\":"
    "\"variant_pageHeadline\",\"text\":\"TableViewer\"}],[\"create\",\"w97\","
    "\"rwt.widgets.Composite\",{\"parent\":\"w95\",\"style\":[\"NONE\"],"
    "\"bounds\":[0,61,1008,525],\"children\":[\"w98\",\"w99\",\"w226\","
    "\"w228\"],\"tabIndex\":-1,\"clientArea\":[0,0,1008,525]}],[\"create\","
    "\"w98\",\"rwt.widgets.Text\",{\"parent\":\"w97\",\"style\":[\"LEFT\","
    "\"SINGLE\",\"BORDER\"],\"bounds\":[10,10,988,32],\"tabIndex\":22,"
    "\"activeKeys\":[\"#13\",\"#27\",\"#40\"]}],[\"listen\",\"w98\",{"
    "\"KeyDown\":true,\"Modify\":true}],[\"create\",\"w99\",\"rwt.widgets."
    "Grid\",{\"parent\":\"w97\",\"style\":[\"SINGLE\",\"BORDER\"],"
    "\"appearance\":\"table\",\"indentionWidth\":0,\"treeColumn\":-1,"
    "\"markupEnabled\":false}],[\"create\",\"w100\",\"rwt.widgets.ScrollBar\",{"
    "\"parent\":\"w99\",\"style\":[\"HORIZONTAL\"]}],[\"create\",\"w101\","
    "\"rwt.widgets.ScrollBar\",{\"parent\":\"w99\",\"style\":[\"VERTICAL\"]}],["
    "\"set\",\"w99\",{\"bounds\":[10,52,988,402],\"children\":[],\"tabIndex\":"
    "23,\"activeKeys\":[\"CTRL+#70\",\"CTRL+#78\",\"CTRL+#82\",\"CTRL+#89\","
    "\"CTRL+#83\",\"CTRL+#71\",\"CTRL+#69\"],\"cancelKeys\":[\"CTRL+#70\","
    "\"CTRL+#78\",\"CTRL+#82\",\"CTRL+#89\",\"CTRL+#83\",\"CTRL+#71\",\"CTRL+#"
    "69\"]}],[\"listen\",\"w99\",{\"MouseDown\":true,\"MouseUp\":true,"
    "\"MouseDoubleClick\":true,\"KeyDown\":true}],[\"set\",\"w99\",{"
    "\"itemCount\":118,\"itemHeight\":28,\"itemMetrics\":[[0,0,50,3,0,3,44],[1,"
    "50,50,53,0,53,44],[2,100,140,103,0,103,134],[3,240,180,243,0,243,174],[4,"
    "420,50,423,0,423,44],[5,470,50,473,0,473,44]],\"columnCount\":6,"
    "\"headerHeight\":35,\"headerVisible\":true,\"linesVisible\":true,"
    "\"focusItem\":\"w108\",\"selection\":[\"w108\"]}],[\"listen\",\"w99\",{"
    "\"Selection\":true,\"DefaultSelection\":true}],[\"set\",\"w99\",{"
    "\"enableCellToolTip\":true}],[\"listen\",\"w100\",{\"Selection\":true}],["
    "\"set\",\"w101\",{\"visibility\":true}],[\"listen\",\"w101\",{"
    "\"Selection\":true}],[\"create\",\"w102\",\"rwt.widgets.GridColumn\",{"
    "\"parent\":\"w99\",\"text\":\"Nr.\",\"width\":50,\"moveable\":true}],["
    "\"listen\",\"w102\",{\"Selection\":true}],[\"create\",\"w103\",\"rwt."
    "widgets.GridColumn\",{\"parent\":\"w99\",\"text\":\"Sym.\",\"index\":1,"
    "\"left\":50,\"width\":50,\"moveable\":true}],[\"listen\",\"w103\",{"
    "\"Selection\":true}],[\"create\",\"w104\",\"rwt.widgets.GridColumn\",{"
    "\"parent\":\"w99\",\"text\":\"Name\",\"index\":2,\"left\":100,\"width\":"
    "140,\"moveable\":true}],[\"listen\",\"w104\",{\"Selection\":true}],["
    "\"create\",\"w105\",\"rwt.widgets.GridColumn\",{\"parent\":\"w99\","
    "\"text\":\"Series\",\"index\":3,\"left\":240,\"width\":180,\"moveable\":"
    "true}],[\"listen\",\"w105\",{\"Selection\":true}],[\"create\",\"w106\","
    "\"rwt.widgets.GridColumn\",{\"parent\":\"w99\",\"text\":\"Group\","
    "\"index\":4,\"left\":420,\"width\":50,\"moveable\":true}],[\"listen\","
    "\"w106\",{\"Selection\":true}],[\"create\",\"w107\",\"rwt.widgets."
    "GridColumn\",{\"parent\":\"w99\",\"text\":\"Period\",\"index\":5,\"left\":"
    "470,\"width\":50,\"moveable\":true}],[\"listen\",\"w107\",{\"Selection\":"
    "true}],[\"create\",\"w108\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":0,\"texts\":[\"1\",\"H\",\"Hydrogen\",\"Nonmetal\",\"1\",\"1\"],"
    "\"cellBackgrounds\":[null,null,null,[138,226,52,255],null,null]}],["
    "\"create\",\"w109\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":1,\"texts\":[\"2\",\"He\",\"Helium\",\"Noble "
    "gas\",\"18\",\"1\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],"
    "null,null]}],[\"create\",\"w110\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":2,\"texts\":[\"3\",\"Li\",\"Lithium\",\"Alkali "
    "metal\",\"1\",\"2\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],"
    "null,null]}],[\"create\",\"w111\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":3,\"texts\":[\"4\",\"Be\",\"Beryllium\",\"Alkaline "
    "earth "
    "metal\",\"2\",\"2\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255]"
    ",null,null]}],[\"create\",\"w112\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":4,\"texts\":[\"5\",\"B\",\"Boron\",\"Metalloid\",\"13\","
    "\"2\"],\"cellBackgrounds\":[null,null,null,[156,159,153,255],null,null]}],"
    "[\"create\",\"w113\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":5,\"texts\":[\"6\",\"C\",\"Carbon\",\"Nonmetal\",\"14\",\"2\"],"
    "\"cellBackgrounds\":[null,null,null,[138,226,52,255],null,null]}],["
    "\"create\",\"w114\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":6,\"texts\":[\"7\",\"N\",\"Nitrogen\",\"Nonmetal\",\"15\",\"2\"]"
    ",\"cellBackgrounds\":[null,null,null,[138,226,52,255],null,null]}],["
    "\"create\",\"w115\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":7,\"texts\":[\"8\",\"O\",\"Oxygen\",\"Nonmetal\",\"16\",\"2\"],"
    "\"cellBackgrounds\":[null,null,null,[138,226,52,255],null,null]}],["
    "\"create\",\"w116\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":8,\"texts\":[\"9\",\"F\",\"Fluorine\",\"Halogen\",\"17\",\"2\"],"
    "\"cellBackgrounds\":[null,null,null,[252,233,79,255],null,null]}],["
    "\"create\",\"w117\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":9,\"texts\":[\"10\",\"Ne\",\"Neon\",\"Noble "
    "gas\",\"18\",\"2\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],"
    "null,null]}],[\"create\",\"w118\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":10,\"texts\":[\"11\",\"Na\",\"Sodium\",\"Alkali "
    "metal\",\"1\",\"3\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],"
    "null,null]}],[\"create\",\"w119\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":11,\"texts\":[\"12\",\"Mg\",\"Magnesium\",\"Alkaline "
    "earth "
    "metal\",\"2\",\"3\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255]"
    ",null,null]}],[\"create\",\"w120\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":12,\"texts\":[\"13\",\"Al\",\"Aluminium\",\"Poor "
    "metal\",\"13\",\"3\"],\"cellBackgrounds\":[null,null,null,[238,238,236,"
    "255],null,null]}],[\"create\",\"w121\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":13,\"texts\":[\"14\",\"Si\",\"Silicon\","
    "\"Metalloid\",\"14\",\"3\"],\"cellBackgrounds\":[null,null,null,[156,159,"
    "153,255],null,null]}],[\"create\",\"w122\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":14,\"texts\":[\"15\",\"P\",\"Phosphorus\","
    "\"Nonmetal\",\"15\",\"3\"],\"cellBackgrounds\":[null,null,null,[138,226,"
    "52,255],null,null]}],[\"create\",\"w123\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":15,\"texts\":[\"16\",\"S\",\"Sulfur\","
    "\"Nonmetal\",\"16\",\"3\"],\"cellBackgrounds\":[null,null,null,[138,226,"
    "52,255],null,null]}],[\"create\",\"w124\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":16,\"texts\":[\"17\",\"Cl\",\"Chlorine\","
    "\"Halogen\",\"17\",\"3\"],\"cellBackgrounds\":[null,null,null,[252,233,79,"
    "255],null,null]}],[\"create\",\"w125\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":17,\"texts\":[\"18\",\"Ar\",\"Argon\","
    "\"Noble "
    "gas\",\"18\",\"3\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],"
    "null,null]}],[\"create\",\"w126\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":18,\"texts\":[\"19\",\"K\",\"Potassium\",\"Alkali "
    "metal\",\"1\",\"4\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],"
    "null,null]}],[\"create\",\"w127\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":19,\"texts\":[\"20\",\"Ca\",\"Calcium\",\"Alkaline "
    "earth "
    "metal\",\"2\",\"4\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255]"
    ",null,null]}],[\"create\",\"w128\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":20,\"texts\":[\"21\",\"Sc\",\"Scandium\",\"Transition "
    "metal\",\"3\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w129\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":21,\"texts\":[\"22\",\"Ti\",\"Titanium\",\"Transition "
    "metal\",\"4\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w130\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":22,\"texts\":[\"23\",\"V\",\"Vanadium\",\"Transition "
    "metal\",\"5\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w131\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":23,\"texts\":[\"24\",\"Cr\",\"Chromium\",\"Transition "
    "metal\",\"6\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w132\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":24,\"texts\":[\"25\",\"Mn\",\"Manganese\",\"Transition "
    "metal\",\"7\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w133\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":25,\"texts\":[\"26\",\"Fe\",\"Iron\",\"Transition "
    "metal\",\"8\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w134\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":26,\"texts\":[\"27\",\"Co\",\"Cobalt\",\"Transition "
    "metal\",\"9\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w135\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":27,\"texts\":[\"28\",\"Ni\",\"Nickel\",\"Transition "
    "metal\",\"10\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w136\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":28,\"texts\":[\"29\",\"Cu\",\"Copper\",\"Transition "
    "metal\",\"11\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w137\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":29,\"texts\":[\"30\",\"Zn\",\"Zinc\",\"Transition "
    "metal\",\"12\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w138\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":30,\"texts\":[\"31\",\"Ga\",\"Gallium\",\"Poor "
    "metal\",\"13\",\"4\"],\"cellBackgrounds\":[null,null,null,[238,238,236,"
    "255],null,null]}],[\"create\",\"w139\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":31,\"texts\":[\"32\",\"Ge\",\"Germanium\","
    "\"Metalloid\",\"14\",\"4\"],\"cellBackgrounds\":[null,null,null,[156,159,"
    "153,255],null,null]}],[\"create\",\"w140\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":32,\"texts\":[\"33\",\"As\",\"Arsenic\","
    "\"Metalloid\",\"15\",\"4\"],\"cellBackgrounds\":[null,null,null,[156,159,"
    "153,255],null,null]}],[\"create\",\"w141\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":33,\"texts\":[\"34\",\"Se\",\"Selenium\","
    "\"Nonmetal\",\"16\",\"4\"],\"cellBackgrounds\":[null,null,null,[138,226,"
    "52,255],null,null]}],[\"create\",\"w142\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":34,\"texts\":[\"35\",\"Br\",\"Bromine\","
    "\"Halogen\",\"17\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,233,79,"
    "255],null,null]}],[\"create\",\"w143\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":35,\"texts\":[\"36\",\"Kr\",\"Krypton\","
    "\"Noble "
    "gas\",\"18\",\"4\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],"
    "null,null]}],[\"create\",\"w144\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":36,\"texts\":[\"37\",\"Rb\",\"Rubidium\",\"Alkali "
    "metal\",\"1\",\"5\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],"
    "null,null]}],[\"create\",\"w145\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":37,\"texts\":[\"38\",\"Sr\",\"Strontium\",\"Alkaline "
    "earth "
    "metal\",\"2\",\"5\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255]"
    ",null,null]}],[\"create\",\"w146\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":38,\"texts\":[\"39\",\"Y\",\"Yttrium\",\"Transition "
    "metal\",\"3\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w147\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":39,\"texts\":[\"40\",\"Zr\",\"Zirconium\",\"Transition "
    "metal\",\"4\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w148\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":40,\"texts\":[\"41\",\"Nb\",\"Niobium\",\"Transition "
    "metal\",\"5\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w149\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":41,\"texts\":[\"42\",\"Mo\",\"Molybdenum\",\"Transition "
    "metal\",\"6\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w150\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":42,\"texts\":[\"43\",\"Tc\",\"Technetium\",\"Transition "
    "metal\",\"7\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w151\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":43,\"texts\":[\"44\",\"Ru\",\"Ruthenium\",\"Transition "
    "metal\",\"8\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w152\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":44,\"texts\":[\"45\",\"Rh\",\"Rhodium\",\"Transition "
    "metal\",\"9\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w153\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":45,\"texts\":[\"46\",\"Pd\",\"Palladium\",\"Transition "
    "metal\",\"10\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w154\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":46,\"texts\":[\"47\",\"Ag\",\"Silver\",\"Transition "
    "metal\",\"11\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w155\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":47,\"texts\":[\"48\",\"Cd\",\"Cadmium\",\"Transition "
    "metal\",\"12\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w156\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":48,\"texts\":[\"49\",\"In\",\"Indium\",\"Poor "
    "metal\",\"13\",\"5\"],\"cellBackgrounds\":[null,null,null,[238,238,236,"
    "255],null,null]}],[\"create\",\"w157\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":49,\"texts\":[\"50\",\"Sn\",\"Tin\",\"Poor "
    "metal\",\"14\",\"5\"],\"cellBackgrounds\":[null,null,null,[238,238,236,"
    "255],null,null]}],[\"create\",\"w158\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":50,\"texts\":[\"51\",\"Sb\",\"Antimony\","
    "\"Metalloid\",\"15\",\"5\"],\"cellBackgrounds\":[null,null,null,[156,159,"
    "153,255],null,null]}],[\"create\",\"w159\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":51,\"texts\":[\"52\",\"Te\",\"Tellurium\","
    "\"Metalloid\",\"16\",\"5\"],\"cellBackgrounds\":[null,null,null,[156,159,"
    "153,255],null,null]}],[\"create\",\"w160\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":52,\"texts\":[\"53\",\"I\",\"Iodine\","
    "\"Halogen\",\"17\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,233,79,"
    "255],null,null]}],[\"create\",\"w161\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":53,\"texts\":[\"54\",\"Xe\",\"Xenon\","
    "\"Noble "
    "gas\",\"18\",\"5\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],"
    "null,null]}],[\"create\",\"w162\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":54,\"texts\":[\"55\",\"Cs\",\"Caesium\",\"Alkali "
    "metal\",\"1\",\"6\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],"
    "null,null]}],[\"create\",\"w163\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":55,\"texts\":[\"56\",\"Ba\",\"Barium\",\"Alkaline earth "
    "metal\",\"2\",\"6\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255]"
    ",null,null]}],[\"create\",\"w164\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":56,\"texts\":[\"57\",\"La\",\"Lanthanum\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w165\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":57,\"texts\":[\"58\",\"Ce\",\"Cerium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w166\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":58,\"texts\":[\"59\",\"Pr\",\"Praseodymium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w167\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":59,\"texts\":[\"60\",\"Nd\",\"Neodymium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w168\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":60,\"texts\":[\"61\",\"Pm\",\"Promethium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w169\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":61,\"texts\":[\"62\",\"Sm\",\"Samarium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w170\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":62,\"texts\":[\"63\",\"Eu\",\"Europium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w171\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":63,\"texts\":[\"64\",\"Gd\",\"Gadolinium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w172\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":64,\"texts\":[\"65\",\"Tb\",\"Terbium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w173\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":65,\"texts\":[\"66\",\"Dy\",\"Dysprosium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w174\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":66,\"texts\":[\"67\",\"Ho\",\"Holmium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w175\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":67,\"texts\":[\"68\",\"Er\",\"Erbium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w176\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":68,\"texts\":[\"69\",\"Tm\",\"Thulium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w177\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":69,\"texts\":[\"70\",\"Yb\",\"Ytterbium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w178\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":70,\"texts\":[\"71\",\"Lu\",\"Lutetium\","
    "\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,"
    "168,255],null,null]}],[\"create\",\"w179\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":71,\"texts\":[\"72\",\"Hf\",\"Hafnium\","
    "\"Transition "
    "metal\",\"4\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w180\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":72,\"texts\":[\"73\",\"Ta\",\"Tantalum\",\"Transition "
    "metal\",\"5\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w181\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":73,\"texts\":[\"74\",\"W\",\"Tungsten\",\"Transition "
    "metal\",\"6\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w182\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":74,\"texts\":[\"75\",\"Re\",\"Rhenium\",\"Transition "
    "metal\",\"7\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w183\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":75,\"texts\":[\"76\",\"Os\",\"Osmium\",\"Transition "
    "metal\",\"8\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w184\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":76,\"texts\":[\"77\",\"Ir\",\"Iridium\",\"Transition "
    "metal\",\"9\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w185\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":77,\"texts\":[\"78\",\"Pt\",\"Platinum\",\"Transition "
    "metal\",\"10\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w186\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":78,\"texts\":[\"79\",\"Au\",\"Gold\",\"Transition "
    "metal\",\"11\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w187\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":79,\"texts\":[\"80\",\"Hg\",\"Mercury\",\"Transition "
    "metal\",\"12\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w188\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":80,\"texts\":[\"81\",\"Tl\",\"Thallium\",\"Poor "
    "metal\",\"13\",\"6\"],\"cellBackgrounds\":[null,null,null,[238,238,236,"
    "255],null,null]}],[\"create\",\"w189\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":81,\"texts\":[\"82\",\"Pb\",\"Lead\",\"Poor "
    "metal\",\"14\",\"6\"],\"cellBackgrounds\":[null,null,null,[238,238,236,"
    "255],null,null]}],[\"create\",\"w190\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":82,\"texts\":[\"83\",\"Bi\",\"Bismuth\","
    "\"Poor "
    "metal\",\"15\",\"6\"],\"cellBackgrounds\":[null,null,null,[238,238,236,"
    "255],null,null]}],[\"create\",\"w191\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":83,\"texts\":[\"84\",\"Po\",\"Polonium\","
    "\"Metalloid\",\"16\",\"6\"],\"cellBackgrounds\":[null,null,null,[156,159,"
    "153,255],null,null]}],[\"create\",\"w192\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":84,\"texts\":[\"85\",\"At\",\"Astatine\","
    "\"Halogen\",\"17\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,233,79,"
    "255],null,null]}],[\"create\",\"w193\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":85,\"texts\":[\"86\",\"Rn\",\"Radon\","
    "\"Noble "
    "gas\",\"18\",\"6\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],"
    "null,null]}],[\"create\",\"w194\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":86,\"texts\":[\"87\",\"Fr\",\"Francium\",\"Alkali "
    "metal\",\"1\",\"7\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],"
    "null,null]}],[\"create\",\"w195\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":87,\"texts\":[\"88\",\"Ra\",\"Radium\",\"Alkaline earth "
    "metal\",\"2\",\"7\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255]"
    ",null,null]}],[\"create\",\"w196\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":88,\"texts\":[\"89\",\"Ac\",\"Actinium\",\"Actinide\","
    "\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,"
    "null]}],[\"create\",\"w197\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":89,\"texts\":[\"90\",\"Th\",\"Thorium\",\"Actinide\",\"3\","
    "\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],"
    "[\"create\",\"w198\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":90,\"texts\":[\"91\",\"Pa\",\"Protactinium\",\"Actinide\",\"3\","
    "\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],"
    "[\"create\",\"w199\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":91,\"texts\":[\"92\",\"U\",\"Uranium\",\"Actinide\",\"3\",\"7\"]"
    ",\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],["
    "\"create\",\"w200\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":92,\"texts\":[\"93\",\"Np\",\"Neptunium\",\"Actinide\",\"3\","
    "\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],"
    "[\"create\",\"w201\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":93,\"texts\":[\"94\",\"Pu\",\"Plutonium\",\"Actinide\",\"3\","
    "\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],"
    "[\"create\",\"w202\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":94,\"texts\":[\"95\",\"Am\",\"Americium\",\"Actinide\",\"3\","
    "\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],"
    "[\"create\",\"w203\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":95,\"texts\":[\"96\",\"Cm\",\"Curium\",\"Actinide\",\"3\",\"7\"]"
    ",\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],["
    "\"create\",\"w204\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":96,\"texts\":[\"97\",\"Bk\",\"Berkelium\",\"Actinide\",\"3\","
    "\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],"
    "[\"create\",\"w205\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":97,\"texts\":[\"98\",\"Cf\",\"Californium\",\"Actinide\",\"3\","
    "\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],"
    "[\"create\",\"w206\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":98,\"texts\":[\"99\",\"Es\",\"Einsteinium\",\"Actinide\",\"3\","
    "\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],"
    "[\"create\",\"w207\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":99,\"texts\":[\"100\",\"Fm\",\"Fermium\",\"Actinide\",\"3\","
    "\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],"
    "[\"create\",\"w208\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":100,\"texts\":[\"101\",\"Md\",\"Mendelevium\",\"Actinide\","
    "\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,"
    "null]}],[\"create\",\"w209\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":101,\"texts\":[\"102\",\"No\",\"Nobelium\",\"Actinide\",\"3\","
    "\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],"
    "[\"create\",\"w210\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":102,\"texts\":[\"103\",\"Lr\",\"Lawrencium\",\"Actinide\",\"3\","
    "\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],"
    "[\"create\",\"w211\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\","
    "\"index\":103,\"texts\":[\"104\",\"Rf\",\"Rutherfordium\",\"Transition "
    "metal\",\"4\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w212\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":104,\"texts\":[\"105\",\"Db\",\"Dubnium\",\"Transition "
    "metal\",\"5\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w213\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":105,\"texts\":[\"106\",\"Sg\",\"Seaborgium\","
    "\"Transition "
    "metal\",\"6\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w214\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":106,\"texts\":[\"107\",\"Bh\",\"Bohrium\",\"Transition "
    "metal\",\"7\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w215\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":107,\"texts\":[\"108\",\"Hs\",\"Hassium\",\"Transition "
    "metal\",\"8\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w216\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":108,\"texts\":[\"109\",\"Mt\",\"Meitnerium\","
    "\"Transition "
    "metal\",\"9\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],"
    "null,null]}],[\"create\",\"w217\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":109,\"texts\":[\"110\",\"Ds\",\"Darmstadtium\","
    "\"Transition "
    "metal\",\"10\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w218\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":110,\"texts\":[\"111\",\"Rg\",\"Roentgenium\","
    "\"Transition "
    "metal\",\"11\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w219\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":111,\"texts\":[\"112\",\"Uub\",\"Ununbium\","
    "\"Transition "
    "metal\",\"12\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255]"
    ",null,null]}],[\"create\",\"w220\",\"rwt.widgets.GridItem\",{\"parent\":"
    "\"w99\",\"index\":112,\"texts\":[\"113\",\"Uut\",\"Ununtrium\",\"Poor "
    "metal\",\"13\",\"7\"],\"cellBackgrounds\":[null,null,null,[238,238,236,"
    "255],null,null]}],[\"create\",\"w221\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":113,\"texts\":[\"114\",\"Uuq\","
    "\"Ununquadium\",\"Poor "
    "metal\",\"14\",\"7\"],\"cellBackgrounds\":[null,null,null,[238,238,236,"
    "255],null,null]}],[\"create\",\"w222\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":114,\"texts\":[\"115\",\"Uup\","
    "\"Ununpentium\",\"Poor "
    "metal\",\"15\",\"7\"],\"cellBackgrounds\":[null,null,null,[238,238,236,"
    "255],null,null]}],[\"create\",\"w223\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":115,\"texts\":[\"116\",\"Uuh\","
    "\"Ununhexium\",\"Poor "
    "metal\",\"16\",\"7\"],\"cellBackgrounds\":[null,null,null,[238,238,236,"
    "255],null,null]}],[\"create\",\"w224\",\"rwt.widgets.GridItem\",{"
    "\"parent\":\"w99\",\"index\":116,\"texts\":[\"117\",\"Uus\","
    "\"Ununseptium\",\"Halogen\",\"17\",\"7\"],\"cellBackgrounds\":[null,null,"
    "null,[252,233,79,255],null,null]}],[\"create\",\"w225\",\"rwt.widgets."
    "GridItem\",{\"parent\":\"w99\",\"index\":117,\"texts\":[\"118\",\"Uuo\","
    "\"Ununoctium\",\"Noble "
    "gas\",\"18\",\"7\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],"
    "null,null]}],[\"create\",\"w226\",\"rwt.widgets.Composite\",{\"parent\":"
    "\"w97\",\"style\":[\"BORDER\"],\"bounds\":[10,464,988,25],\"children\":["
    "\"w227\"],\"tabIndex\":-1,\"clientArea\":[0,0,986,23]}],[\"create\","
    "\"w227\",\"rwt.widgets.Label\",{\"parent\":\"w226\",\"style\":[\"NONE\"],"
    "\"bounds\":[10,10,966,3],\"tabIndex\":-1,\"text\":\"Hydrogen "
    "(H)\"}],[\"create\",\"w228\",\"rwt.widgets.Label\",{\"parent\":\"w97\","
    "\"style\":[\"WRAP\"],\"bounds\":[10,499,988,16],\"tabIndex\":-1,"
    "\"foreground\":[150,150,150,255],\"font\":[[\"Verdana\",\"Lucida "
    "Sans\",\"Arial\",\"Helvetica\",\"sans-serif\"],10,false,false],\"text\":"
    "\"Shortcuts: [CTRL+F] - Filter | Sort by: [CTRL+R] - Number, [CTRL+Y] - "
    "Symbol, [CTRL+N] - Name, [CTRL+S] - Series, [CTRL+G] - Group, [CTRL+E] - "
    "Period\"}],[\"set\",\"w1\",{\"focusControl\":\"w99\"}],[\"call\",\"rwt."
    "client.BrowserNavigation\",\"addToHistory\",{\"entries\":[["
    "\"tableviewer\",\"TableViewer\"]]}]]}";

class HashIndexTable {
  std::array<int32_t, 32> _hashTable{};

 public:
  void add(const std::string& name, int32_t index) {
    const int32_t slot = hashSlotFor(name);
    if (index < 0xff) {
      // increment by 1, 0 stands for empty
      _hashTable[slot] = (index + 1) & 0xff;
    } else {
      _hashTable[slot] = 0;
    }
  }

  [[nodiscard]] int32_t get(const std::string& name) const {
    const int32_t slot = hashSlotFor(name);
    // subtract 1, 0 stands for empty
    return (_hashTable[slot] & 0xff) - 1;
  }

 private:
  [[nodiscard]] int32_t stringHash(const std::string& s) const {
    // this is not a proper hash, but sufficient for the benchmark,
    // and very portable!
    return static_cast<int32_t>(s.size()) * 1402589;
  }

  [[nodiscard]] int32_t hashSlotFor(const std::string& element) const {
    return stringHash(element) & (static_cast<int32_t>(_hashTable.size()) - 1);
  }
};

class JsonArray;
class JsonObject;

class JsonValue {
 public:
  JsonValue() = default;
  virtual ~JsonValue() = default;

  [[nodiscard]] virtual bool isObject() const { return false; }
  [[nodiscard]] virtual bool isArray() const { return false; }
  [[nodiscard]] virtual bool isNumber() const { return false; }
  [[nodiscard]] virtual bool isString() const { return false; }
  [[nodiscard]] virtual bool isBoolean() const { return false; }
  [[nodiscard]] virtual bool isTrue() const { return false; }
  [[nodiscard]] virtual bool isFalse() const { return false; }
  [[nodiscard]] virtual bool isNull() const { return false; }

  [[nodiscard]] virtual const JsonObject* asObject() const {
    throw Error("Not an object: " + toString());
  }

  [[nodiscard]] virtual const JsonArray* asArray() const {
    throw Error("Not an array: " + toString());
  }

  [[nodiscard]] virtual string toString() const { return ""; }
};

class JsonArray : public JsonValue {
 private:
  Vector<const JsonValue*> _values{};

 public:
  JsonArray() = default;
  ~JsonArray() override { _values.destroyValues(); }

  void add(const JsonValue* value) {
    if (value == nullptr) {
      throw Error("value is null");
    }
    _values.append(value);
  }

  [[nodiscard]] size_t size() const { return _values.size(); }

  [[nodiscard]] const JsonValue* get(int32_t index) const {
    return *_values.at(index);
  }

  [[nodiscard]] bool isArray() const override { return true; }

  [[nodiscard]] const JsonArray* asArray() const override { return this; }
};

class JsonLiteral : public JsonValue {
 private:
  string _value;
  bool _isNull;
  bool _isTrue;
  bool _isFalse;

  JsonLiteral(string value, bool isNull, bool isTrue, bool isFalse)
      : _value(std::move(value)),
        _isNull(isNull),
        _isTrue(isTrue),
        _isFalse(isFalse) {}

 public:
  explicit JsonLiteral(const string& value)
      : _value(value),
        _isNull(value == "null"),
        _isTrue(value == "true"),
        _isFalse(value == "false") {}

  [[nodiscard]] bool isNull() const override { return _isNull; }
  [[nodiscard]] bool isTrue() const override { return _isTrue; }
  [[nodiscard]] bool isFalse() const override { return _isFalse; }
  [[nodiscard]] bool isBoolean() const override { return _isTrue || _isFalse; }

  [[nodiscard]] string toString() const override { return _value; }

  static JsonLiteral* createNull() {
    return new JsonLiteral("null", true, false, false);
  }

  static JsonLiteral* createTrue() {
    return new JsonLiteral("true", false, true, false);
  }

  static JsonLiteral* createFalse() {
    return new JsonLiteral("false", false, false, true);
  }
};

class JsonNumber : public JsonValue {
 private:
  string _string;

 public:
  explicit JsonNumber(const string& string) : _string(string) {
    if (string.empty()) {
      throw Error("value is null");
    }
  }

  [[nodiscard]] string toString() const override { return _string; }
  [[nodiscard]] bool isNumber() const override { return true; }
};

class JsonObject : public JsonValue {
 private:
  Vector<string> _names{};
  Vector<const JsonValue*> _values{};
  HashIndexTable _table{};

  [[nodiscard]] int32_t indexOf(const string& name) const {
    const int32_t index = _table.get(name);
    if (index != -1 && name == *_names.at(index)) {
      return index;
    }
    throw Error("not yet implemented, not relevant for the benchmark");
  }

 public:
  JsonObject() = default;
  ~JsonObject() override { _values.destroyValues(); }

  void add(const string& name, const JsonValue* value) {
    if (name.empty()) {
      throw Error("name is null");
    }
    if (value == nullptr) {
      throw Error("value is null");
    }
    _table.add(name, static_cast<int32_t>(_names.size()));
    _names.append(name);
    _values.append(value);
  }

  [[nodiscard]] const JsonValue* get(const string& name) const {
    if (name.empty()) {
      throw Error("name is null");
    }
    const int32_t index = indexOf(name);
    return index == -1 ? nullptr : *_values.at(index);
  }

  [[nodiscard]] size_t size() const { return _names.size(); }
  [[nodiscard]] bool isEmpty() const { return _names.isEmpty(); }
  [[nodiscard]] bool isObject() const override { return true; }
  [[nodiscard]] const JsonObject* asObject() const override { return this; }
};

class JsonString : public JsonValue {
 private:
  string _string;

 public:
  explicit JsonString(string string) : _string(std::move(string)) {}

  [[nodiscard]] bool isString() const override { return true; }
};

class ParseException : virtual public std::exception {
 private:
  size_t _offset;
  int32_t _line;
  int32_t _column;
  string _what;

 public:
  ParseException(const string& message,
                 size_t offset,
                 int32_t line,
                 int32_t column)
      : _offset(offset),
        _line(line),
        _column(column),
        _what(message + " at " + std::to_string(line) + ":" +
              std::to_string(column)) {}

  [[nodiscard]] size_t getOffset() const { return _offset; }
  [[nodiscard]] int32_t getLine() const { return _line; }
  [[nodiscard]] int32_t getColumn() const { return _column; }
  [[nodiscard]] const char* what() const noexcept override {
    return (_what.c_str());
  }
};

class JsonPureStringParser {
 private:
  string _input;
  size_t _index{SIZE_MAX};
  int32_t _line{1};
  int32_t _column{0};
  string _current{};
  string _captureBuffer{};
  size_t _captureStart{SIZE_MAX};

  const JsonValue* readValue() {
    if (_current == "n") {
      return readNull();
    }
    if (_current == "t") {
      return readTrue();
    }
    if (_current == "f") {
      return readFalse();
    }
    if (_current == "\"") {
      return readString();
    }
    if (_current == "[") {
      return readArray();
    }
    if (_current == "{") {
      return readObject();
    }
    if (_current == "-" || _current == "0" || _current == "1" ||
        _current == "2" || _current == "3" || _current == "4" ||
        _current == "5" || _current == "6" || _current == "7" ||
        _current == "8" || _current == "9") {
      return readNumber();
    }

    throw expected("value");
  }

  JsonObject* readObject() {
    read();
    auto* object = new JsonObject();
    skipWhiteSpace();
    if (readChar("}")) {
      return object;
    }
    do {
      skipWhiteSpace();
      const string name = readName();
      skipWhiteSpace();
      if (!readChar(":")) {
        throw expected("':'");
      }
      skipWhiteSpace();

      object->add(name, readValue());
      skipWhiteSpace();
    } while (readChar(","));
    if (!readChar("}")) {
      throw expected("',' or '}'");
    }
    return object;
  }

  string readName() {
    if (_current != "\"") {
      throw expected("name");
    }
    return readStringInternal();
  }

  JsonArray* readArray() {
    read();
    auto* array = new JsonArray();
    skipWhiteSpace();
    if (readChar("]")) {
      return array;
    }
    do {
      skipWhiteSpace();
      array->add(readValue());
      skipWhiteSpace();
    } while (readChar(","));

    if (!readChar("]")) {
      throw expected("',' or ']'");
    }
    return array;
  }

  const JsonValue* readNull() {
    read();
    readRequiredChar("u");
    readRequiredChar("l");
    readRequiredChar("l");
    return JsonLiteral::createNull();
  }

  const JsonValue* readTrue() {
    read();
    readRequiredChar("r");
    readRequiredChar("u");
    readRequiredChar("e");
    return JsonLiteral::createTrue();
  }

  const JsonValue* readFalse() {
    read();
    readRequiredChar("a");
    readRequiredChar("l");
    readRequiredChar("s");
    readRequiredChar("e");
    return JsonLiteral::createFalse();
  }

  void readRequiredChar(const string& ch) {
    if (!readChar(ch)) {
      throw expected("'" + ch + "'");
    }
  }

  const JsonValue* readString() { return new JsonString(readStringInternal()); }

  string readStringInternal() {
    read();
    startCapture();
    while (_current != "\"") {
      if (_current == "\\") {
        pauseCapture();
        readEscape();
        startCapture();
      } else {
        read();
      }
    }
    string string = endCapture();
    read();
    return string;
  }

  void readEscape() {
    read();
    if (_current == "\"" || _current == "/" || _current == "\\") {
      _captureBuffer += _current;
    } else if (_current == "b") {
      _captureBuffer += "\b";
    } else if (_current == "f") {
      _captureBuffer += "\f";
    } else if (_current == "n") {
      _captureBuffer += "\n";
    } else if (_current == "r") {
      _captureBuffer += "\r";
    } else if (_current == "t") {
      _captureBuffer += "\t";
    } else {
      throw expected("valid escape sequence");
    }
    read();
  }

  const JsonValue* readNumber() {
    startCapture();
    readChar("-");
    const string firstDigit = _current;
    if (!readDigit()) {
      throw expected("digit");
    }
    if (firstDigit != "0") {
      while (readDigit()) {
      }
    }
    readFraction();
    readExponent();
    return new JsonNumber(endCapture());
  }

  bool readFraction() {
    if (!readChar(".")) {
      return false;
    }
    if (!readDigit()) {
      throw expected("digit");
    }
    while (readDigit()) {
    }

    return true;
  }

  bool readExponent() {
    if (!readChar("e") && !readChar("E")) {
      return false;
    }
    if (!readChar("+")) {
      readChar("-");
    }
    if (!readDigit()) {
      throw expected("digit");
    }

    while (readDigit()) {
    }
    return true;
  }

  bool readChar(const string& ch) {
    if (_current != ch) {
      return false;
    }
    read();
    return true;
  }

  bool readDigit() {
    if (!isDigit()) {
      return false;
    }
    read();
    return true;
  }

  void skipWhiteSpace() {
    while (isWhiteSpace()) {
      read();
    }
  }

  void read() {
    if ("\n" == _current) {
      _line += 1;
      _column = 0;
    }
    _index += 1;
    if (_index < _input.length()) {
      _current = _input.substr(_index, 1);
    } else {
      _current = "";
    }
  }

  void startCapture() { _captureStart = _index; }

  void pauseCapture() {
    const size_t _end = _current.empty() ? _index : _index - 1;
    _captureBuffer += _input.substr(_captureStart, _end - _captureStart + 1);
    _captureStart = -1;
  }

  string endCapture() {
    const size_t _end = _current.empty() ? _index : _index - 1;
    string captured;
    if (_captureBuffer.empty()) {
      captured = _input.substr(_captureStart, _end - _captureStart + 1);
    } else {
      _captureBuffer += _input.substr(_captureStart, _end - _captureStart + 1);
      captured = _captureBuffer;
      _captureBuffer = "";
    }
    _captureStart = -1;
    return captured;
  }

  ParseException expected(const string& expected) {
    if (isEndOfText()) {
      return error("Unexpected end of input");
    }

    return error("Expected " + expected);
  }

  [[nodiscard]] ParseException error(const string& message) const {
    return {message, _index, _line, _column - 1};
  }

  bool isWhiteSpace() {
    return " " == _current || "\t" == _current || "\n" == _current ||
           "\r" == _current;
  }

  bool isDigit() {
    return "0" == _current || "1" == _current || "2" == _current ||
           "3" == _current || "4" == _current || "5" == _current ||
           "6" == _current || "7" == _current || "8" == _current ||
           "9" == _current;
  }

  bool isEndOfText() { return _current.empty(); }

 public:
  explicit JsonPureStringParser(string string) : _input(std::move(string)) {}

  const JsonValue* parse() {
    read();
    skipWhiteSpace();
    const JsonValue* result = readValue();
    skipWhiteSpace();
    if (!isEndOfText()) {
      throw Error("Unexpected character");
    }
    return result;
  }
};

class Json : public Benchmark {
 private:
 public:
  std::any benchmark() override {
    JsonPureStringParser parser{rapBenchmarkMinified};
    const JsonValue* const result = parser.parse();
    return result;
  }

  bool has_expected_content(const JsonValue* result) {
    if (!result->isObject()) {
      return false;
    }
    const auto* resultObject = result->asObject();
    if (!resultObject->get("head")->isObject()) {
      return false;
    }
    if (!resultObject->get("operations")->isArray()) {
      return false;
    }
    const auto* resultArray = resultObject->get("operations")->asArray();
    return resultArray->size() == 156;
  }

  bool verify_result(std::any r) override {
    const auto* const result = std::any_cast<const JsonValue*>(r);
    const bool doesVerify = has_expected_content(result);
    delete result;
    return doesVerify;
  }
};
