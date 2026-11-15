using Benchmarks.Som;

namespace Benchmarks;

internal class Json : Benchmark
{
    public override object Execute() => new JsonPureStringParser(RapBenchmarkMinified).Parse();

    private const string RapBenchmarkMinified = "{\"head\":{\"requestCounter\":4},\"operations\":[[\"destroy\",\"w54\"],[\"set\",\"w2\",{\"activeControl\":\"w99\"}],[\"set\",\"w21\",{\"customVariant\":\"variant_navigation\"}],[\"set\",\"w28\",{\"customVariant\":\"variant_selected\"}],[\"set\",\"w53\",{\"children\":[\"w95\"]}],[\"create\",\"w95\",\"rwt.widgets.Composite\",{\"parent\":\"w53\",\"style\":[\"NONE\"],\"bounds\":[0,0,1008,586],\"children\":[\"w96\",\"w97\"],\"tabIndex\":-1,\"clientArea\":[0,0,1008,586]}],[\"create\",\"w96\",\"rwt.widgets.Label\",{\"parent\":\"w95\",\"style\":[\"NONE\"],\"bounds\":[10,30,112,26],\"tabIndex\":-1,\"customVariant\":\"variant_pageHeadline\",\"text\":\"TableViewer\"}],[\"create\",\"w97\",\"rwt.widgets.Composite\",{\"parent\":\"w95\",\"style\":[\"NONE\"],\"bounds\":[0,61,1008,525],\"children\":[\"w98\",\"w99\",\"w226\",\"w228\"],\"tabIndex\":-1,\"clientArea\":[0,0,1008,525]}],[\"create\",\"w98\",\"rwt.widgets.Text\",{\"parent\":\"w97\",\"style\":[\"LEFT\",\"SINGLE\",\"BORDER\"],\"bounds\":[10,10,988,32],\"tabIndex\":22,\"activeKeys\":[\"#13\",\"#27\",\"#40\"]}],[\"listen\",\"w98\",{\"KeyDown\":true,\"Modify\":true}],[\"create\",\"w99\",\"rwt.widgets.Grid\",{\"parent\":\"w97\",\"style\":[\"SINGLE\",\"BORDER\"],\"appearance\":\"table\",\"indentionWidth\":0,\"treeColumn\":-1,\"markupEnabled\":false}],[\"create\",\"w100\",\"rwt.widgets.ScrollBar\",{\"parent\":\"w99\",\"style\":[\"HORIZONTAL\"]}],[\"create\",\"w101\",\"rwt.widgets.ScrollBar\",{\"parent\":\"w99\",\"style\":[\"VERTICAL\"]}],[\"set\",\"w99\",{\"bounds\":[10,52,988,402],\"children\":[],\"tabIndex\":23,\"activeKeys\":[\"CTRL+#70\",\"CTRL+#78\",\"CTRL+#82\",\"CTRL+#89\",\"CTRL+#83\",\"CTRL+#71\",\"CTRL+#69\"],\"cancelKeys\":[\"CTRL+#70\",\"CTRL+#78\",\"CTRL+#82\",\"CTRL+#89\",\"CTRL+#83\",\"CTRL+#71\",\"CTRL+#69\"]}],[\"listen\",\"w99\",{\"MouseDown\":true,\"MouseUp\":true,\"MouseDoubleClick\":true,\"KeyDown\":true}],[\"set\",\"w99\",{\"itemCount\":118,\"itemHeight\":28,\"itemMetrics\":[[0,0,50,3,0,3,44],[1,50,50,53,0,53,44],[2,100,140,103,0,103,134],[3,240,180,243,0,243,174],[4,420,50,423,0,423,44],[5,470,50,473,0,473,44]],\"columnCount\":6,\"headerHeight\":35,\"headerVisible\":true,\"linesVisible\":true,\"focusItem\":\"w108\",\"selection\":[\"w108\"]}],[\"listen\",\"w99\",{\"Selection\":true,\"DefaultSelection\":true}],[\"set\",\"w99\",{\"enableCellToolTip\":true}],[\"listen\",\"w100\",{\"Selection\":true}],[\"set\",\"w101\",{\"visibility\":true}],[\"listen\",\"w101\",{\"Selection\":true}],[\"create\",\"w102\",\"rwt.widgets.GridColumn\",{\"parent\":\"w99\",\"text\":\"Nr.\",\"width\":50,\"moveable\":true}],[\"listen\",\"w102\",{\"Selection\":true}],[\"create\",\"w103\",\"rwt.widgets.GridColumn\",{\"parent\":\"w99\",\"text\":\"Sym.\",\"index\":1,\"left\":50,\"width\":50,\"moveable\":true}],[\"listen\",\"w103\",{\"Selection\":true}],[\"create\",\"w104\",\"rwt.widgets.GridColumn\",{\"parent\":\"w99\",\"text\":\"Name\",\"index\":2,\"left\":100,\"width\":140,\"moveable\":true}],[\"listen\",\"w104\",{\"Selection\":true}],[\"create\",\"w105\",\"rwt.widgets.GridColumn\",{\"parent\":\"w99\",\"text\":\"Series\",\"index\":3,\"left\":240,\"width\":180,\"moveable\":true}],[\"listen\",\"w105\",{\"Selection\":true}],[\"create\",\"w106\",\"rwt.widgets.GridColumn\",{\"parent\":\"w99\",\"text\":\"Group\",\"index\":4,\"left\":420,\"width\":50,\"moveable\":true}],[\"listen\",\"w106\",{\"Selection\":true}],[\"create\",\"w107\",\"rwt.widgets.GridColumn\",{\"parent\":\"w99\",\"text\":\"Period\",\"index\":5,\"left\":470,\"width\":50,\"moveable\":true}],[\"listen\",\"w107\",{\"Selection\":true}],[\"create\",\"w108\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":0,\"texts\":[\"1\",\"H\",\"Hydrogen\",\"Nonmetal\",\"1\",\"1\"],\"cellBackgrounds\":[null,null,null,[138,226,52,255],null,null]}],[\"create\",\"w109\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":1,\"texts\":[\"2\",\"He\",\"Helium\",\"Noble gas\",\"18\",\"1\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],null,null]}],[\"create\",\"w110\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":2,\"texts\":[\"3\",\"Li\",\"Lithium\",\"Alkali metal\",\"1\",\"2\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],null,null]}],[\"create\",\"w111\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":3,\"texts\":[\"4\",\"Be\",\"Beryllium\",\"Alkaline earth metal\",\"2\",\"2\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255],null,null]}],[\"create\",\"w112\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":4,\"texts\":[\"5\",\"B\",\"Boron\",\"Metalloid\",\"13\",\"2\"],\"cellBackgrounds\":[null,null,null,[156,159,153,255],null,null]}],[\"create\",\"w113\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":5,\"texts\":[\"6\",\"C\",\"Carbon\",\"Nonmetal\",\"14\",\"2\"],\"cellBackgrounds\":[null,null,null,[138,226,52,255],null,null]}],[\"create\",\"w114\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":6,\"texts\":[\"7\",\"N\",\"Nitrogen\",\"Nonmetal\",\"15\",\"2\"],\"cellBackgrounds\":[null,null,null,[138,226,52,255],null,null]}],[\"create\",\"w115\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":7,\"texts\":[\"8\",\"O\",\"Oxygen\",\"Nonmetal\",\"16\",\"2\"],\"cellBackgrounds\":[null,null,null,[138,226,52,255],null,null]}],[\"create\",\"w116\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":8,\"texts\":[\"9\",\"F\",\"Fluorine\",\"Halogen\",\"17\",\"2\"],\"cellBackgrounds\":[null,null,null,[252,233,79,255],null,null]}],[\"create\",\"w117\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":9,\"texts\":[\"10\",\"Ne\",\"Neon\",\"Noble gas\",\"18\",\"2\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],null,null]}],[\"create\",\"w118\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":10,\"texts\":[\"11\",\"Na\",\"Sodium\",\"Alkali metal\",\"1\",\"3\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],null,null]}],[\"create\",\"w119\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":11,\"texts\":[\"12\",\"Mg\",\"Magnesium\",\"Alkaline earth metal\",\"2\",\"3\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255],null,null]}],[\"create\",\"w120\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":12,\"texts\":[\"13\",\"Al\",\"Aluminium\",\"Poor metal\",\"13\",\"3\"],\"cellBackgrounds\":[null,null,null,[238,238,236,255],null,null]}],[\"create\",\"w121\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":13,\"texts\":[\"14\",\"Si\",\"Silicon\",\"Metalloid\",\"14\",\"3\"],\"cellBackgrounds\":[null,null,null,[156,159,153,255],null,null]}],[\"create\",\"w122\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":14,\"texts\":[\"15\",\"P\",\"Phosphorus\",\"Nonmetal\",\"15\",\"3\"],\"cellBackgrounds\":[null,null,null,[138,226,52,255],null,null]}],[\"create\",\"w123\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":15,\"texts\":[\"16\",\"S\",\"Sulfur\",\"Nonmetal\",\"16\",\"3\"],\"cellBackgrounds\":[null,null,null,[138,226,52,255],null,null]}],[\"create\",\"w124\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":16,\"texts\":[\"17\",\"Cl\",\"Chlorine\",\"Halogen\",\"17\",\"3\"],\"cellBackgrounds\":[null,null,null,[252,233,79,255],null,null]}],[\"create\",\"w125\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":17,\"texts\":[\"18\",\"Ar\",\"Argon\",\"Noble gas\",\"18\",\"3\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],null,null]}],[\"create\",\"w126\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":18,\"texts\":[\"19\",\"K\",\"Potassium\",\"Alkali metal\",\"1\",\"4\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],null,null]}],[\"create\",\"w127\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":19,\"texts\":[\"20\",\"Ca\",\"Calcium\",\"Alkaline earth metal\",\"2\",\"4\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255],null,null]}],[\"create\",\"w128\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":20,\"texts\":[\"21\",\"Sc\",\"Scandium\",\"Transition metal\",\"3\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w129\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":21,\"texts\":[\"22\",\"Ti\",\"Titanium\",\"Transition metal\",\"4\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w130\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":22,\"texts\":[\"23\",\"V\",\"Vanadium\",\"Transition metal\",\"5\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w131\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":23,\"texts\":[\"24\",\"Cr\",\"Chromium\",\"Transition metal\",\"6\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w132\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":24,\"texts\":[\"25\",\"Mn\",\"Manganese\",\"Transition metal\",\"7\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w133\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":25,\"texts\":[\"26\",\"Fe\",\"Iron\",\"Transition metal\",\"8\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w134\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":26,\"texts\":[\"27\",\"Co\",\"Cobalt\",\"Transition metal\",\"9\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w135\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":27,\"texts\":[\"28\",\"Ni\",\"Nickel\",\"Transition metal\",\"10\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w136\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":28,\"texts\":[\"29\",\"Cu\",\"Copper\",\"Transition metal\",\"11\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w137\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":29,\"texts\":[\"30\",\"Zn\",\"Zinc\",\"Transition metal\",\"12\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w138\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":30,\"texts\":[\"31\",\"Ga\",\"Gallium\",\"Poor metal\",\"13\",\"4\"],\"cellBackgrounds\":[null,null,null,[238,238,236,255],null,null]}],[\"create\",\"w139\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":31,\"texts\":[\"32\",\"Ge\",\"Germanium\",\"Metalloid\",\"14\",\"4\"],\"cellBackgrounds\":[null,null,null,[156,159,153,255],null,null]}],[\"create\",\"w140\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":32,\"texts\":[\"33\",\"As\",\"Arsenic\",\"Metalloid\",\"15\",\"4\"],\"cellBackgrounds\":[null,null,null,[156,159,153,255],null,null]}],[\"create\",\"w141\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":33,\"texts\":[\"34\",\"Se\",\"Selenium\",\"Nonmetal\",\"16\",\"4\"],\"cellBackgrounds\":[null,null,null,[138,226,52,255],null,null]}],[\"create\",\"w142\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":34,\"texts\":[\"35\",\"Br\",\"Bromine\",\"Halogen\",\"17\",\"4\"],\"cellBackgrounds\":[null,null,null,[252,233,79,255],null,null]}],[\"create\",\"w143\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":35,\"texts\":[\"36\",\"Kr\",\"Krypton\",\"Noble gas\",\"18\",\"4\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],null,null]}],[\"create\",\"w144\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":36,\"texts\":[\"37\",\"Rb\",\"Rubidium\",\"Alkali metal\",\"1\",\"5\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],null,null]}],[\"create\",\"w145\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":37,\"texts\":[\"38\",\"Sr\",\"Strontium\",\"Alkaline earth metal\",\"2\",\"5\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255],null,null]}],[\"create\",\"w146\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":38,\"texts\":[\"39\",\"Y\",\"Yttrium\",\"Transition metal\",\"3\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w147\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":39,\"texts\":[\"40\",\"Zr\",\"Zirconium\",\"Transition metal\",\"4\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w148\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":40,\"texts\":[\"41\",\"Nb\",\"Niobium\",\"Transition metal\",\"5\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w149\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":41,\"texts\":[\"42\",\"Mo\",\"Molybdenum\",\"Transition metal\",\"6\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w150\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":42,\"texts\":[\"43\",\"Tc\",\"Technetium\",\"Transition metal\",\"7\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w151\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":43,\"texts\":[\"44\",\"Ru\",\"Ruthenium\",\"Transition metal\",\"8\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w152\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":44,\"texts\":[\"45\",\"Rh\",\"Rhodium\",\"Transition metal\",\"9\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w153\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":45,\"texts\":[\"46\",\"Pd\",\"Palladium\",\"Transition metal\",\"10\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w154\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":46,\"texts\":[\"47\",\"Ag\",\"Silver\",\"Transition metal\",\"11\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w155\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":47,\"texts\":[\"48\",\"Cd\",\"Cadmium\",\"Transition metal\",\"12\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w156\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":48,\"texts\":[\"49\",\"In\",\"Indium\",\"Poor metal\",\"13\",\"5\"],\"cellBackgrounds\":[null,null,null,[238,238,236,255],null,null]}],[\"create\",\"w157\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":49,\"texts\":[\"50\",\"Sn\",\"Tin\",\"Poor metal\",\"14\",\"5\"],\"cellBackgrounds\":[null,null,null,[238,238,236,255],null,null]}],[\"create\",\"w158\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":50,\"texts\":[\"51\",\"Sb\",\"Antimony\",\"Metalloid\",\"15\",\"5\"],\"cellBackgrounds\":[null,null,null,[156,159,153,255],null,null]}],[\"create\",\"w159\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":51,\"texts\":[\"52\",\"Te\",\"Tellurium\",\"Metalloid\",\"16\",\"5\"],\"cellBackgrounds\":[null,null,null,[156,159,153,255],null,null]}],[\"create\",\"w160\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":52,\"texts\":[\"53\",\"I\",\"Iodine\",\"Halogen\",\"17\",\"5\"],\"cellBackgrounds\":[null,null,null,[252,233,79,255],null,null]}],[\"create\",\"w161\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":53,\"texts\":[\"54\",\"Xe\",\"Xenon\",\"Noble gas\",\"18\",\"5\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],null,null]}],[\"create\",\"w162\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":54,\"texts\":[\"55\",\"Cs\",\"Caesium\",\"Alkali metal\",\"1\",\"6\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],null,null]}],[\"create\",\"w163\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":55,\"texts\":[\"56\",\"Ba\",\"Barium\",\"Alkaline earth metal\",\"2\",\"6\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255],null,null]}],[\"create\",\"w164\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":56,\"texts\":[\"57\",\"La\",\"Lanthanum\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w165\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":57,\"texts\":[\"58\",\"Ce\",\"Cerium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w166\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":58,\"texts\":[\"59\",\"Pr\",\"Praseodymium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w167\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":59,\"texts\":[\"60\",\"Nd\",\"Neodymium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w168\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":60,\"texts\":[\"61\",\"Pm\",\"Promethium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w169\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":61,\"texts\":[\"62\",\"Sm\",\"Samarium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w170\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":62,\"texts\":[\"63\",\"Eu\",\"Europium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w171\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":63,\"texts\":[\"64\",\"Gd\",\"Gadolinium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w172\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":64,\"texts\":[\"65\",\"Tb\",\"Terbium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w173\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":65,\"texts\":[\"66\",\"Dy\",\"Dysprosium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w174\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":66,\"texts\":[\"67\",\"Ho\",\"Holmium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w175\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":67,\"texts\":[\"68\",\"Er\",\"Erbium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w176\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":68,\"texts\":[\"69\",\"Tm\",\"Thulium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w177\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":69,\"texts\":[\"70\",\"Yb\",\"Ytterbium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w178\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":70,\"texts\":[\"71\",\"Lu\",\"Lutetium\",\"Lanthanide\",\"3\",\"6\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w179\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":71,\"texts\":[\"72\",\"Hf\",\"Hafnium\",\"Transition metal\",\"4\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w180\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":72,\"texts\":[\"73\",\"Ta\",\"Tantalum\",\"Transition metal\",\"5\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w181\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":73,\"texts\":[\"74\",\"W\",\"Tungsten\",\"Transition metal\",\"6\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w182\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":74,\"texts\":[\"75\",\"Re\",\"Rhenium\",\"Transition metal\",\"7\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w183\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":75,\"texts\":[\"76\",\"Os\",\"Osmium\",\"Transition metal\",\"8\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w184\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":76,\"texts\":[\"77\",\"Ir\",\"Iridium\",\"Transition metal\",\"9\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w185\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":77,\"texts\":[\"78\",\"Pt\",\"Platinum\",\"Transition metal\",\"10\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w186\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":78,\"texts\":[\"79\",\"Au\",\"Gold\",\"Transition metal\",\"11\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w187\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":79,\"texts\":[\"80\",\"Hg\",\"Mercury\",\"Transition metal\",\"12\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w188\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":80,\"texts\":[\"81\",\"Tl\",\"Thallium\",\"Poor metal\",\"13\",\"6\"],\"cellBackgrounds\":[null,null,null,[238,238,236,255],null,null]}],[\"create\",\"w189\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":81,\"texts\":[\"82\",\"Pb\",\"Lead\",\"Poor metal\",\"14\",\"6\"],\"cellBackgrounds\":[null,null,null,[238,238,236,255],null,null]}],[\"create\",\"w190\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":82,\"texts\":[\"83\",\"Bi\",\"Bismuth\",\"Poor metal\",\"15\",\"6\"],\"cellBackgrounds\":[null,null,null,[238,238,236,255],null,null]}],[\"create\",\"w191\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":83,\"texts\":[\"84\",\"Po\",\"Polonium\",\"Metalloid\",\"16\",\"6\"],\"cellBackgrounds\":[null,null,null,[156,159,153,255],null,null]}],[\"create\",\"w192\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":84,\"texts\":[\"85\",\"At\",\"Astatine\",\"Halogen\",\"17\",\"6\"],\"cellBackgrounds\":[null,null,null,[252,233,79,255],null,null]}],[\"create\",\"w193\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":85,\"texts\":[\"86\",\"Rn\",\"Radon\",\"Noble gas\",\"18\",\"6\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],null,null]}],[\"create\",\"w194\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":86,\"texts\":[\"87\",\"Fr\",\"Francium\",\"Alkali metal\",\"1\",\"7\"],\"cellBackgrounds\":[null,null,null,[239,41,41,255],null,null]}],[\"create\",\"w195\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":87,\"texts\":[\"88\",\"Ra\",\"Radium\",\"Alkaline earth metal\",\"2\",\"7\"],\"cellBackgrounds\":[null,null,null,[233,185,110,255],null,null]}],[\"create\",\"w196\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":88,\"texts\":[\"89\",\"Ac\",\"Actinium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w197\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":89,\"texts\":[\"90\",\"Th\",\"Thorium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w198\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":90,\"texts\":[\"91\",\"Pa\",\"Protactinium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w199\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":91,\"texts\":[\"92\",\"U\",\"Uranium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w200\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":92,\"texts\":[\"93\",\"Np\",\"Neptunium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w201\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":93,\"texts\":[\"94\",\"Pu\",\"Plutonium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w202\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":94,\"texts\":[\"95\",\"Am\",\"Americium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w203\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":95,\"texts\":[\"96\",\"Cm\",\"Curium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w204\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":96,\"texts\":[\"97\",\"Bk\",\"Berkelium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w205\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":97,\"texts\":[\"98\",\"Cf\",\"Californium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w206\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":98,\"texts\":[\"99\",\"Es\",\"Einsteinium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w207\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":99,\"texts\":[\"100\",\"Fm\",\"Fermium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w208\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":100,\"texts\":[\"101\",\"Md\",\"Mendelevium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w209\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":101,\"texts\":[\"102\",\"No\",\"Nobelium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w210\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":102,\"texts\":[\"103\",\"Lr\",\"Lawrencium\",\"Actinide\",\"3\",\"7\"],\"cellBackgrounds\":[null,null,null,[173,127,168,255],null,null]}],[\"create\",\"w211\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":103,\"texts\":[\"104\",\"Rf\",\"Rutherfordium\",\"Transition metal\",\"4\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w212\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":104,\"texts\":[\"105\",\"Db\",\"Dubnium\",\"Transition metal\",\"5\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w213\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":105,\"texts\":[\"106\",\"Sg\",\"Seaborgium\",\"Transition metal\",\"6\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w214\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":106,\"texts\":[\"107\",\"Bh\",\"Bohrium\",\"Transition metal\",\"7\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w215\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":107,\"texts\":[\"108\",\"Hs\",\"Hassium\",\"Transition metal\",\"8\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w216\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":108,\"texts\":[\"109\",\"Mt\",\"Meitnerium\",\"Transition metal\",\"9\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w217\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":109,\"texts\":[\"110\",\"Ds\",\"Darmstadtium\",\"Transition metal\",\"10\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w218\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":110,\"texts\":[\"111\",\"Rg\",\"Roentgenium\",\"Transition metal\",\"11\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w219\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":111,\"texts\":[\"112\",\"Uub\",\"Ununbium\",\"Transition metal\",\"12\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,175,62,255],null,null]}],[\"create\",\"w220\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":112,\"texts\":[\"113\",\"Uut\",\"Ununtrium\",\"Poor metal\",\"13\",\"7\"],\"cellBackgrounds\":[null,null,null,[238,238,236,255],null,null]}],[\"create\",\"w221\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":113,\"texts\":[\"114\",\"Uuq\",\"Ununquadium\",\"Poor metal\",\"14\",\"7\"],\"cellBackgrounds\":[null,null,null,[238,238,236,255],null,null]}],[\"create\",\"w222\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":114,\"texts\":[\"115\",\"Uup\",\"Ununpentium\",\"Poor metal\",\"15\",\"7\"],\"cellBackgrounds\":[null,null,null,[238,238,236,255],null,null]}],[\"create\",\"w223\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":115,\"texts\":[\"116\",\"Uuh\",\"Ununhexium\",\"Poor metal\",\"16\",\"7\"],\"cellBackgrounds\":[null,null,null,[238,238,236,255],null,null]}],[\"create\",\"w224\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":116,\"texts\":[\"117\",\"Uus\",\"Ununseptium\",\"Halogen\",\"17\",\"7\"],\"cellBackgrounds\":[null,null,null,[252,233,79,255],null,null]}],[\"create\",\"w225\",\"rwt.widgets.GridItem\",{\"parent\":\"w99\",\"index\":117,\"texts\":[\"118\",\"Uuo\",\"Ununoctium\",\"Noble gas\",\"18\",\"7\"],\"cellBackgrounds\":[null,null,null,[114,159,207,255],null,null]}],[\"create\",\"w226\",\"rwt.widgets.Composite\",{\"parent\":\"w97\",\"style\":[\"BORDER\"],\"bounds\":[10,464,988,25],\"children\":[\"w227\"],\"tabIndex\":-1,\"clientArea\":[0,0,986,23]}],[\"create\",\"w227\",\"rwt.widgets.Label\",{\"parent\":\"w226\",\"style\":[\"NONE\"],\"bounds\":[10,10,966,3],\"tabIndex\":-1,\"text\":\"Hydrogen (H)\"}],[\"create\",\"w228\",\"rwt.widgets.Label\",{\"parent\":\"w97\",\"style\":[\"WRAP\"],\"bounds\":[10,499,988,16],\"tabIndex\":-1,\"foreground\":[150,150,150,255],\"font\":[[\"Verdana\",\"Lucida Sans\",\"Arial\",\"Helvetica\",\"sans-serif\"],10,false,false],\"text\":\"Shortcuts: [CTRL+F] - Filter | Sort by: [CTRL+R] - Number, [CTRL+Y] - Symbol, [CTRL+N] - Name, [CTRL+S] - Series, [CTRL+G] - Group, [CTRL+E] - Period\"}],[\"set\",\"w1\",{\"focusControl\":\"w99\"}],[\"call\",\"rwt.client.BrowserNavigation\",\"addToHistory\",{\"entries\":[[\"tableviewer\",\"TableViewer\"]]}]]}";


    public override bool VerifyResult(object r)
    {
        JsonValue result = (JsonValue)r;

        if (!result.IsObject())
        {
            return false;
        }

        if (!result.AsObject().Get("head")!.IsObject())
        {
            return false;
        }

        if (!result.AsObject().Get("operations")!.IsArray())
        {
            return false;
        }

        return result.AsObject().Get("operations")!.AsArray().Size() == 156;
    }
}

/// <summary>
/// Represents a JSON array, an ordered collection of JSON values.
/// </summary>
/// <remarks>
/// Elements can be added using the <c>Add(...)</c> methods which accept instances of
/// <see cref="JsonValue"/>, strings, primitive numbers, and bool values. To replace an element of an
/// array, use the <c>Set(int, ...)</c> methods.
///
/// Note that this class is <b>not thread-safe</b>. If multiple threads access a
/// <see cref="JsonArray"/> instance concurrently, while at least one of these threads modifies the
/// contents of this array, access to the instance must be synchronized externally. Failure to do so
/// may lead to an inconsistent state.
///
/// This class is <b>not supposed to be extended</b> by clients.
/// </remarks>
internal sealed class JsonArray : JsonValue
{
    private readonly Vector<JsonValue> _values;

    /// <summary>
    /// Creates a new empty JsonArray.
    /// </summary>
    public JsonArray()
    {
        _values = new Vector<JsonValue>();
    }

    /// <summary>
    /// Appends the specified JSON value to the end of this array.
    /// </summary>
    /// <param name="value">the JsonValue to add to the array, must not be <c>null</c></param>
    /// <returns>the array itself, to enable method chaining</returns>
    public JsonArray Add(JsonValue value)
    {
        if (value == null)
        {
            throw new ArgumentNullException(nameof(value), "JsonValue must not be null");
        }

        _values.Append(value);
        return this;
    }

    /// <summary>
    /// Returns the number of elements in this array.
    /// </summary>
    /// <returns>the number of elements in this array</returns>
    public int Size() => _values.Size();

    /// <summary>
    /// Returns the value of the element at the specified position in this array.
    /// </summary>
    /// <param name="index">the index of the array element to return</param>
    /// <returns>the value of the element at the specified position</returns>
    /// <exception cref="IndexOutOfRangeException">if the index is out of range, i.e. <c>index &lt; 0</c> or <c>index &gt;= size</c></exception>
    public JsonValue? Get(int index) => _values.At(index);

    public override bool IsArray() => true;

    public override JsonArray AsArray() => this;
}

internal sealed class JsonLiteral : JsonValue
{
    public static readonly JsonValue Null = new JsonLiteral("null");
    public static readonly JsonValue True = new JsonLiteral("true");
    public static readonly JsonValue False = new JsonLiteral("false");

    private readonly string _value;
    private readonly bool _isNull;
    private readonly bool _isTrue;
    private readonly bool _isFalse;

    public JsonLiteral(string value)
    {
        _value = value;
        _isNull = value == "null";
        _isTrue = value == "true";
        _isFalse = value == "false";
    }

    public override string ToString() => _value;

    public override bool IsNull() => _isNull;

    public override bool IsTrue() => _isTrue;

    public override bool IsFalse() => _isFalse;

    public override bool IsBool() => _isTrue || _isFalse;
}

internal sealed class JsonNumber : JsonValue
{
    private readonly string _str;

    public JsonNumber(string str)
    {
        _str = str;
        if (str == null)
        {
            throw new ArgumentNullException(nameof(str), "String must not be null");
        }
    }

    public override string ToString() => _str;

    public override bool IsNumber() => true;
}

/// <summary>
/// Represents a JSON object, a set of name/value pairs, where the names are strings and the values
/// are JSON values.
/// </summary>
/// <remarks>
/// Members can be added using the <c>Add(string, ...)</c> methods which accept instances of
/// <see cref="JsonValue"/>, strings, primitive numbers, and bool values. To modify certain values of an
/// object, use the <c>Set(string, ...)</c> methods. Please note that the <c>Add</c>
/// methods are faster than <c>Set</c> as they do not search for existing members. On the other
/// hand, the <c>Add</c> methods do not prevent adding multiple members with the same name.
/// Duplicate names are discouraged but not prohibited by JSON.
///
/// Members can be accessed by their name using <see cref="Get(string)"/>.
///
/// This class is <b>not supposed to be extended</b> by clients.
/// </remarks>
internal sealed class JsonObject : JsonValue
{
    private readonly Vector<string> _names;
    private readonly Vector<JsonValue> _values;
    private readonly HashIndexTable _table;

    /// <summary>
    /// Creates a new empty JsonObject.
    /// </summary>
    public JsonObject()
    {
        _names = new Vector<string>();
        _values = new Vector<JsonValue>();
        _table = new HashIndexTable();
    }

    /// <summary>
    /// Appends a new member to the end of this object, with the specified name and the specified JSON
    /// value.
    /// </summary>
    /// <remarks>
    /// This method <b>does not prevent duplicate names</b>. Calling this method with a name
    /// that already exists in the object will append another member with the same name. In order to
    /// replace existing members, use the method <c>Set(name, value)</c> instead. However,
    /// <b><i>Add</i> is much faster than <i>Set</i></b> (because it does not need to
    /// search for existing members). Therefore <i>Add</i> should be preferred when constructing new
    /// objects.
    /// </remarks>
    /// <param name="name">the name of the member to add</param>
    /// <param name="value">the value of the member to add, must not be <c>null</c></param>
    /// <returns>the object itself, to enable method chaining</returns>
    public JsonObject Add(string name, JsonValue value)
    {
        if (name == null)
        {
            throw new ArgumentNullException(nameof(name), "name must not be null");
        }

        if (value == null)
        {
            throw new ArgumentNullException(nameof(value), "value must not be null");
        }

        _table.Add(name, _names.Size());
        _names.Append(name);
        _values.Append(value);
        return this;
    }

    /// <summary>
    /// Returns the value of the member with the specified name in this object. If this object contains
    /// multiple members with the given name, this method will return the last one.
    /// </summary>
    /// <param name="name">the name of the member whose value is to be returned</param>
    /// <returns>the value of the last member with the specified name, or <c>null</c> if this
    ///         object does not contain a member with that name</returns>
    public JsonValue? Get(string name)
    {
        if (name == null)
        {
            throw new ArgumentNullException(nameof(name), "name must not be null");
        }

        int index = IndexOf(name);
        return index == -1 ? null : _values.At(index);
    }

    /// <summary>
    /// Returns the number of members (name/value pairs) in this object.
    /// </summary>
    /// <returns>the number of members in this object</returns>
    public int Size() => _names.Size();

    /// <summary>
    /// Returns <c>true</c> if this object contains no members.
    /// </summary>
    /// <returns><c>true</c> if this object contains no members</returns>
    public bool IsEmpty() => _names.IsEmpty();

    public override bool IsObject() => true;

    public override JsonObject AsObject() => this;

    private int IndexOf(string name)
    {
        int index = _table.Get(name);
        if (index != -1 && name == _names.At(index))
        {
            return index;
        }

        throw new NotImplementedException(); // Not needed for benchmark
    }

    private sealed class HashIndexTable
    {
        private readonly int[] _hashTable = new int[32]; // must be a power of two

        public void Add(string name, int index)
        {
            int slot = HashSlotFor(name);
            if (index < 0xff)
            {
                // increment by 1, 0 stands for empty
                _hashTable[slot] = (index + 1) & 0xff;
            }
            else
            {
                _hashTable[slot] = 0;
            }
        }

        public int Get(string name)
        {
            int slot = HashSlotFor(name);
            // subtract 1, 0 stands for empty
            return (_hashTable[slot] & 0xff) - 1;
        }

        private int StringHash(string s)
        {
            // this is not a proper hash, but sufficient for the benchmark,
            // and very portable!
            return s.Length * 1402589;
        }

        private int HashSlotFor(string element)
        {
            return StringHash(element) & (_hashTable.Length - 1);
        }
    }
}

internal sealed class JsonPureStringParser
{
    private readonly string _input;
    private int _index;
    private int _line;
    private int _column;
    private string? _current;
    private string _captureBuffer;
    private int _captureStart;

    public JsonPureStringParser(string str)
    {
        _input = str;
        _index = -1;
        _line = 1;
        _captureStart = -1;
        _column = 0;
        _current = null;
        _captureBuffer = "";
    }

    public JsonValue Parse()
    {
        Read();
        SkipWhiteSpace();
        JsonValue result = ReadValue();
        SkipWhiteSpace();
        if (!IsEndOfText())
        {
            throw Error("Unexpected character");
        }

        return result;
    }

    private JsonValue ReadValue()
    {
        switch (_current)
        {
            case "n":
                return ReadNull();
            case "t":
                return ReadTrue();
            case "f":
                return ReadFalse();
            case "\"":
                return ReadString();
            case "[":
                return ReadArray();
            case "{":
                return ReadObject();
            case "-":
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
                return ReadNumber();
            default:
                throw Expected("value");
        }
    }

    private JsonArray ReadArray()
    {
        Read();
        JsonArray array = new();
        SkipWhiteSpace();
        if (ReadChar("]"))
        {
            return array;
        }

        do
        {
            SkipWhiteSpace();
            array.Add(ReadValue());
            SkipWhiteSpace();
        } while (ReadChar(","));

        if (!ReadChar("]"))
        {
            throw Expected("',' or ']'");
        }

        return array;
    }

    private JsonObject ReadObject()
    {
        Read();
        JsonObject obj = new();
        SkipWhiteSpace();
        if (ReadChar("}"))
        {
            return obj;
        }

        do
        {
            SkipWhiteSpace();
            string name = ReadName();
            SkipWhiteSpace();
            if (!ReadChar(":"))
            {
                throw Expected("':'");
            }

            SkipWhiteSpace();
            obj.Add(name, ReadValue());
            SkipWhiteSpace();
        } while (ReadChar(","));

        if (!ReadChar("}"))
        {
            throw Expected("',' or '}'");
        }

        return obj;
    }

    private string ReadName()
    {
        if (_current != "\"")
        {
            throw Expected("name");
        }

        return ReadStringInternal();
    }

    private JsonValue ReadNull()
    {
        Read();
        ReadRequiredChar("u");
        ReadRequiredChar("l");
        ReadRequiredChar("l");
        return JsonLiteral.Null;
    }

    private JsonValue ReadTrue()
    {
        Read();
        ReadRequiredChar("r");
        ReadRequiredChar("u");
        ReadRequiredChar("e");
        return JsonLiteral.True;
    }

    private JsonValue ReadFalse()
    {
        Read();
        ReadRequiredChar("a");
        ReadRequiredChar("l");
        ReadRequiredChar("s");
        ReadRequiredChar("e");
        return JsonLiteral.False;
    }

    private void ReadRequiredChar(string ch)
    {
        if (!ReadChar(ch))
        {
            throw Expected("'" + ch + "'");
        }
    }

    private JsonValue ReadString() => new JsonString(ReadStringInternal());

    private string ReadStringInternal()
    {
        Read();
        StartCapture();
        while (_current != "\"")
        {
            if (_current == "\\")
            {
                PauseCapture();
                ReadEscape();
                StartCapture();
            }
            else
            {
                Read();
            }
        }

        string str = EndCapture();
        Read();
        return str;
    }

    private void ReadEscape()
    {
        Read();
        switch (_current)
        {
            case "\"":
            case "/":
            case "\\":
                _captureBuffer += _current;
                break;
            case "b":
                _captureBuffer += "\b";
                break;
            case "f":
                _captureBuffer += "\f";
                break;
            case "n":
                _captureBuffer += "\n";
                break;
            case "r":
                _captureBuffer += "\r";
                break;
            case "t":
                _captureBuffer += "\t";
                break;
            default:
                throw Expected("valid escape sequence");
        }

        Read();
    }

    private JsonValue ReadNumber()
    {
        StartCapture();
        ReadChar("-");
        string? firstDigit = _current;
        if (!ReadDigit())
        {
            throw Expected("digit");
        }

        if (firstDigit != "0")
        {
            while (ReadDigit())
            {
            }
        }

        ReadFraction();
        ReadExponent();
        return new JsonNumber(EndCapture());
    }

    private bool ReadFraction()
    {
        if (!ReadChar("."))
        {
            return false;
        }

        if (!ReadDigit())
        {
            throw Expected("digit");
        }

        while (ReadDigit())
        {
        }

        return true;
    }

    private bool ReadExponent()
    {
        if (!ReadChar("e") && !ReadChar("E"))
        {
            return false;
        }

        if (!ReadChar("+"))
        {
            ReadChar("-");
        }

        if (!ReadDigit())
        {
            throw Expected("digit");
        }

        while (ReadDigit())
        {
        }

        return true;
    }

    private bool ReadChar(string ch)
    {
        if (_current != ch)
        {
            return false;
        }

        Read();
        return true;
    }

    private bool ReadDigit()
    {
        if (!IsDigit())
        {
            return false;
        }

        Read();
        return true;
    }

    private void SkipWhiteSpace()
    {
        while (IsWhiteSpace())
        {
            Read();
        }
    }

    private void Read()
    {
        if (_current == "\n")
        {
            _line++;
            _column = 0;
        }

        _index++;

        _current = _index < _input.Length ? _input.Substring(_index, 1) : null;
    }

    private void StartCapture()
    {
        _captureStart = _index;
    }

    private void PauseCapture()
    {
        int end = _current == null ? _index : _index - 1;
        _captureBuffer += _input[_captureStart..(end + 1)];
        _captureStart = -1;
    }

    private string EndCapture()
    {
        int end = _current == null ? _index : _index - 1;
        string captured;
        if (_captureBuffer.Length == 0)
        {
            captured = _input[_captureStart..(end + 1)];
        }
        else
        {
            _captureBuffer += _input[_captureStart..(end + 1)];
            captured = _captureBuffer;
            _captureBuffer = "";
        }

        _captureStart = -1;
        return captured;
    }

    private ParseException Expected(string expected)
    {
        if (IsEndOfText())
        {
            return Error("Unexpected end of input");
        }

        return Error($"Expected {expected}");
    }

    private ParseException Error(string message) => new(message, _index, _line, _column - 1);

    private bool IsWhiteSpace() => 
        _current == " " || 
        _current == "\t" || 
        _current == "\n" || 
        _current == "\r";

    private bool IsDigit() =>
        _current == "0" ||
        _current == "1" ||
        _current == "2" ||
        _current == "3" ||
        _current == "4" ||
        _current == "5" ||
        _current == "6" ||
        _current == "7" ||
        _current == "8" ||
        _current == "9";

    private bool IsEndOfText() => _current == null;
}

internal sealed class JsonString : JsonValue
{
    private readonly string _str;

    public JsonString(string str)
    {
        _str = str;
    }

    public override bool IsString() => true;
}

/// <summary>
/// Represents a JSON value. This can be a JSON <see cref="JsonObject"/> (object), an <see cref="JsonArray"/> (array),
/// a <b>number</b>, a <b>string</b>, or one of the literals <c>true</c>, <c>false</c>, and <c>null</c>.
/// </summary>
/// <remarks>
/// The literals <c>true</c>, <c>false</c>, and <c>null</c> are represented by the constants <c>TRUE</c>, <c>FALSE</c>, and <c>NULL</c>.
/// JSON objects and arrays are represented by the subtypes <see cref="JsonObject"/> and <see cref="JsonArray"/>. Instances of these types can be created using the
/// public constructors of these classes.
/// 
/// Instances that represent JSON <b>numbers</b>, <b>strings</b> and <b>bool</b> values can be created using the static factory methods
/// <c>valueOf(string)</c>, <c>valueOf(long)</c>, <c>valueOf(double)</c>, etc.
/// 
/// In order to find out whether an instance of this class is of a certain type, the methods
/// <see cref="IsObject()"/>, <see cref="IsArray()"/>, <see cref="IsString()"/>, <see cref="IsNumber()"/> etc. can be
/// used.
/// 
/// If the type of a JSON value is known, the methods <see cref="AsObject()"/>, <see cref="AsArray()"/>,
/// <c>AsString()</c>, <c>AsInt()</c>, etc. can be used to get this value directly in the
/// appropriate target type.
/// 
/// This class is <b>not supposed to be extended</b> by clients.
/// </remarks>
internal abstract class JsonValue
{
    private protected JsonValue()
    {
        // prevent subclasses outside of this package
    }

    /// <summary>
    /// Detects whether this value represents a JSON object. If this is the case, this value is an
    /// instance of <see cref="JsonObject"/>.
    /// </summary>
    /// <returns><c>true</c> if this value is an instance of JsonObject</returns>
    public virtual bool IsObject() => false;

    /// <summary>
    /// Detects whether this value represents a JSON array. If this is the case, this value is an
    /// instance of <see cref="JsonArray"/>.
    /// </summary>
    /// <returns><c>true</c> if this value is an instance of JsonArray</returns>
    public virtual bool IsArray() => false;

    /// <summary>
    /// Detects whether this value represents a JSON number.
    /// </summary>
    /// <returns><c>true</c> if this value represents a JSON number</returns>
    public virtual bool IsNumber() => false;

    /// <summary>
    /// Detects whether this value represents a JSON string.
    /// </summary>
    /// <returns><c>true</c> if this value represents a JSON string</returns>
    public virtual bool IsString() => false;

    /// <summary>
    /// Detects whether this value represents a bool value.
    /// </summary>
    /// <returns><c>true</c> if this value represents either the JSON literal <c>true</c> or <c>false</c></returns>
    public virtual bool IsBool() => false;

    /// <summary>
    /// Detects whether this value represents the JSON literal <c>true</c>.
    /// </summary>
    /// <returns><c>true</c> if this value represents the JSON literal <c>true</c></returns>
    public virtual bool IsTrue() => false;

    /// <summary>
    /// Detects whether this value represents the JSON literal <c>false</c>.
    /// </summary>
    /// <returns><c>true</c> if this value represents the JSON literal <c>false</c></returns>
    public virtual bool IsFalse() => false;

    /// <summary>
    /// Detects whether this value represents the JSON literal <c>null</c>.
    /// </summary>
    /// <returns><c>true</c> if this value represents the JSON literal <c>null</c></returns>
    public virtual bool IsNull() => false;

    /// <summary>
    /// Returns this JSON value as <see cref="JsonObject"/>, assuming that this value represents a JSON
    /// object. If this is not the case, an exception is thrown.
    /// </summary>
    /// <returns>a <see cref="JsonObject"/> for this value</returns>
    /// <exception cref="InvalidOperationException">if this value is not a JSON object</exception>
    public virtual JsonObject AsObject() => throw new InvalidOperationException($"Not an object: {ToString()}");

    /// <summary>
    /// Returns this JSON value as <see cref="JsonArray"/>, assuming that this value represents a JSON array.
    /// If this is not the case, an exception is thrown.
    /// </summary>
    /// <returns>a <see cref="JsonArray"/> for this value</returns>
    /// <exception cref="InvalidOperationException">if this value is not a JSON array</exception>
    public virtual JsonArray AsArray() => throw new InvalidOperationException($"Not an array: {ToString()}");
}

internal class ParseException : Exception
{
    public int Offset { get; }
    public int Line { get; }
    public int Column { get; }

    public ParseException(string message, int offset, int line, int column) : base($"{message} at {line}:{column}")
    {
        Offset = offset;
        Line = line;
        Column = column;
    }
}
