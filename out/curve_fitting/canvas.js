// Compiled by ClojureScript 1.10.339 {}
goog.provide('curve_fitting.canvas');
goog.require('cljs.core');
goog.require('reagent.core');
goog.require('re_frame.core');
cljs.core.enable_console_print_BANG_.call(null);
curve_fitting.canvas.transform_context = (function curve_fitting$canvas$transform_context(ctx,center_x,center_y,scale_x,scale_y){
ctx.translate(center_x,center_y);

ctx.scale(scale_x,scale_y);

return ctx;
});
curve_fitting.canvas.add_relations = (function curve_fitting$canvas$add_relations(config){
var c = config;
var c__$1 = cljs.core.assoc.call(null,c,new cljs.core.Keyword(null,"range-x","range-x",-1724389287),(new cljs.core.Keyword(null,"max-x","max-x",1609536425).cljs$core$IFn$_invoke$arity$1(c) - new cljs.core.Keyword(null,"min-x","min-x",-1544012261).cljs$core$IFn$_invoke$arity$1(c)),new cljs.core.Keyword(null,"range-y","range-y",-541153491),(new cljs.core.Keyword(null,"max-y","max-y",1525628082).cljs$core$IFn$_invoke$arity$1(c) - new cljs.core.Keyword(null,"min-y","min-y",-1969872948).cljs$core$IFn$_invoke$arity$1(c)));
return cljs.core.assoc.call(null,c__$1,new cljs.core.Keyword(null,"unit-x","unit-x",-841417847),(new cljs.core.Keyword(null,"width","width",-384071477).cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"canvas","canvas",-1798817489).cljs$core$IFn$_invoke$arity$1(c__$1)) / new cljs.core.Keyword(null,"range-x","range-x",-1724389287).cljs$core$IFn$_invoke$arity$1(c__$1)),new cljs.core.Keyword(null,"unit-y","unit-y",-235486817),(new cljs.core.Keyword(null,"height","height",1025178622).cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"canvas","canvas",-1798817489).cljs$core$IFn$_invoke$arity$1(c__$1)) / new cljs.core.Keyword(null,"range-y","range-y",-541153491).cljs$core$IFn$_invoke$arity$1(c__$1)),new cljs.core.Keyword(null,"center-x","center-x",2109659472),Math.round(Math.abs(((new cljs.core.Keyword(null,"min-x","min-x",-1544012261).cljs$core$IFn$_invoke$arity$1(c__$1) / new cljs.core.Keyword(null,"range-x","range-x",-1724389287).cljs$core$IFn$_invoke$arity$1(c__$1)) * new cljs.core.Keyword(null,"canvas","canvas",-1798817489).cljs$core$IFn$_invoke$arity$1(c__$1).width))),new cljs.core.Keyword(null,"center-y","center-y",-233780987),Math.round(Math.abs(((new cljs.core.Keyword(null,"min-y","min-y",-1969872948).cljs$core$IFn$_invoke$arity$1(c__$1) / new cljs.core.Keyword(null,"range-y","range-y",-541153491).cljs$core$IFn$_invoke$arity$1(c__$1)) * new cljs.core.Keyword(null,"canvas","canvas",-1798817489).cljs$core$IFn$_invoke$arity$1(c__$1).height))),new cljs.core.Keyword(null,"iteration","iteration",-1088952049),(new cljs.core.Keyword(null,"range-x","range-x",-1724389287).cljs$core$IFn$_invoke$arity$1(c__$1) / (1000)),new cljs.core.Keyword(null,"scale-x","scale-x",-13535878),(new cljs.core.Keyword(null,"canvas","canvas",-1798817489).cljs$core$IFn$_invoke$arity$1(c__$1).width / new cljs.core.Keyword(null,"range-x","range-x",-1724389287).cljs$core$IFn$_invoke$arity$1(c__$1)),new cljs.core.Keyword(null,"scale-y","scale-y",1326124277),(new cljs.core.Keyword(null,"canvas","canvas",-1798817489).cljs$core$IFn$_invoke$arity$1(c__$1).height / new cljs.core.Keyword(null,"range-y","range-y",-541153491).cljs$core$IFn$_invoke$arity$1(c__$1)));
});
curve_fitting.canvas.make_graph = (function curve_fitting$canvas$make_graph(){
var canvas = document.getElementById("plot");
var graph = curve_fitting.canvas.add_relations.call(null,new cljs.core.PersistentArrayMap(null, 6, [new cljs.core.Keyword(null,"canvas","canvas",-1798817489),canvas,new cljs.core.Keyword(null,"min-x","min-x",-1544012261),(-10),new cljs.core.Keyword(null,"min-y","min-y",-1969872948),(-10),new cljs.core.Keyword(null,"max-x","max-x",1609536425),(10),new cljs.core.Keyword(null,"max-y","max-y",1525628082),(10),new cljs.core.Keyword(null,"units-per-tick","units-per-tick",-1345919743),(1)], null));
var ctx = new cljs.core.Keyword(null,"canvas","canvas",-1798817489).cljs$core$IFn$_invoke$arity$1(graph).getContext("2d");
ctx.globalCompositeOperation = "multiply";

return cljs.core.assoc.call(null,graph,new cljs.core.Keyword(null,"context","context",-830191113),curve_fitting.canvas.transform_context.call(null,ctx,new cljs.core.Keyword(null,"center-x","center-x",2109659472).cljs$core$IFn$_invoke$arity$1(graph),new cljs.core.Keyword(null,"center-y","center-y",-233780987).cljs$core$IFn$_invoke$arity$1(graph),new cljs.core.Keyword(null,"scale-x","scale-x",-13535878).cljs$core$IFn$_invoke$arity$1(graph),new cljs.core.Keyword(null,"scale-y","scale-y",1326124277).cljs$core$IFn$_invoke$arity$1(graph)));
});
curve_fitting.canvas.add_equation_BANG_ = (function curve_fitting$canvas$add_equation_BANG_(graph,equation,opacity){
var ctx = new cljs.core.Keyword(null,"context","context",-830191113).cljs$core$IFn$_invoke$arity$1(graph);
ctx.globalAlpha = (opacity / (100));

ctx.beginPath();

ctx.moveTo(new cljs.core.Keyword(null,"min-x","min-x",-1544012261).cljs$core$IFn$_invoke$arity$1(graph),equation.call(null,new cljs.core.Keyword(null,"min-x","min-x",-1544012261).cljs$core$IFn$_invoke$arity$1(graph)));

var x_1165 = new cljs.core.Keyword(null,"min-x","min-x",-1544012261).cljs$core$IFn$_invoke$arity$1(graph);
while(true){
if((x_1165 < new cljs.core.Keyword(null,"max-x","max-x",1609536425).cljs$core$IFn$_invoke$arity$1(graph))){
ctx.lineTo(x_1165,equation.call(null,x_1165));

var G__1166 = (x_1165 + new cljs.core.Keyword(null,"iteration","iteration",-1088952049).cljs$core$IFn$_invoke$arity$1(graph));
x_1165 = G__1166;
continue;
} else {
}
break;
}

ctx.lineJoin = "round";

ctx.lineWidth = ((2) / new cljs.core.Keyword(null,"scale-x","scale-x",-13535878).cljs$core$IFn$_invoke$arity$1(graph));

ctx.strokeStyle = "grey";

ctx.stroke();

return ctx;
});
curve_fitting.canvas.add_point_BANG_ = (function curve_fitting$canvas$add_point_BANG_(graph,coords){
var ctx = new cljs.core.Keyword(null,"context","context",-830191113).cljs$core$IFn$_invoke$arity$1(graph);
var vec__1167 = coords.tail;
var x_coord = cljs.core.nth.call(null,vec__1167,(0),null);
var y_coord = cljs.core.nth.call(null,vec__1167,(1),null);
ctx.globalAlpha = 1.0;

ctx.beginPath();

ctx.arc(x_coord,y_coord,((3) / new cljs.core.Keyword(null,"scale-x","scale-x",-13535878).cljs$core$IFn$_invoke$arity$1(graph)),(0),(Math.PI * (2)),true);

ctx.fill();

return ctx;
});
re_frame.core.reg_event_db.call(null,new cljs.core.Keyword(null,"initialize","initialize",609952913),(function (_,___$1){
return new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"equations","equations",1884238648),cljs.core.PersistentVector.EMPTY,new cljs.core.Keyword(null,"animate","animate",1850194573),false,new cljs.core.Keyword(null,"points","points",-1486596883),cljs.core.PersistentVector.EMPTY], null);
}));
curve_fitting.canvas.re_trigger_timer = (function curve_fitting$canvas$re_trigger_timer(){
return reagent.core.next_tick.call(null,(function (){
return re_frame.core.dispatch.call(null,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"timer","timer",-1266967739)], null));
}));
});
curve_fitting.canvas.convert_scales = (function curve_fitting$canvas$convert_scales(event,graph){
var rect = new cljs.core.Keyword(null,"canvas","canvas",-1798817489).cljs$core$IFn$_invoke$arity$1(graph).getBoundingClientRect();
var x_pixel_val = (event.nativeEvent.clientX - goog.object.get(rect,"left"));
var y_pixel_val = (event.nativeEvent.clientY - goog.object.get(rect,"top"));
var x_data_val = ((x_pixel_val - new cljs.core.Keyword(null,"center-x","center-x",2109659472).cljs$core$IFn$_invoke$arity$1(graph)) / new cljs.core.Keyword(null,"scale-x","scale-x",-13535878).cljs$core$IFn$_invoke$arity$1(graph));
var y_data_val = ((y_pixel_val - new cljs.core.Keyword(null,"center-y","center-y",-233780987).cljs$core$IFn$_invoke$arity$1(graph)) / new cljs.core.Keyword(null,"scale-y","scale-y",1326124277).cljs$core$IFn$_invoke$arity$1(graph));
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x_data_val,y_data_val], null);
});
re_frame.core.reg_event_db.call(null,new cljs.core.Keyword(null,"timer","timer",-1266967739),(function (db,_){
if(cljs.core.truth_(new cljs.core.Keyword(null,"animate","animate",1850194573).cljs$core$IFn$_invoke$arity$1(db))){
curve_fitting.canvas.re_trigger_timer.call(null);

return cljs.core.assoc.call(null,db,new cljs.core.Keyword(null,"equations","equations",1884238648),cljs.core.map.call(null,(function (p1__1170_SHARP_){
return cljs.core.update.call(null,p1__1170_SHARP_,new cljs.core.Keyword(null,"opacity","opacity",397153780),cljs.core.dec);
}),cljs.core.filter.call(null,(function (p1__1171_SHARP_){
return (new cljs.core.Keyword(null,"opacity","opacity",397153780).cljs$core$IFn$_invoke$arity$1(p1__1171_SHARP_) > (1));
}),new cljs.core.Keyword(null,"equations","equations",1884238648).cljs$core$IFn$_invoke$arity$1(db))));
} else {
return db;
}
}));
re_frame.core.reg_event_db.call(null,new cljs.core.Keyword(null,"new-eq","new-eq",-1545781695),(function (db,p__1172){
var vec__1173 = p__1172;
var _ = cljs.core.nth.call(null,vec__1173,(0),null);
var eq = cljs.core.nth.call(null,vec__1173,(1),null);
return cljs.core.update_in.call(null,db,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"equations","equations",1884238648)], null),cljs.core.conj,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"equation","equation",-499527745),eq,new cljs.core.Keyword(null,"opacity","opacity",397153780),(100)], null));
}));
re_frame.core.reg_event_db.call(null,new cljs.core.Keyword(null,"click","click",1912301393),(function (db,p__1176){
var vec__1177 = p__1176;
var _ = cljs.core.nth.call(null,vec__1177,(0),null);
var coords = cljs.core.nth.call(null,vec__1177,(1),null);
var x = cljs.core.first.call(null,goog.object.get(coords,"tail"));
var y = cljs.core.second.call(null,goog.object.get(coords,"tail"));
return cljs.core.update_in.call(null,db,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"points","points",-1486596883)], null),cljs.core.conj,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [x,y], null));
}));
re_frame.core.reg_event_fx.call(null,new cljs.core.Keyword(null,"toggle-animation","toggle-animation",2103108973),(function (cofx,_){
var db = new cljs.core.Keyword(null,"db","db",993250759).cljs$core$IFn$_invoke$arity$1(cofx);
var animating = new cljs.core.Keyword(null,"animate","animate",1850194573).cljs$core$IFn$_invoke$arity$1(db);
var disp = (cljs.core.truth_(animating)?cljs.core.PersistentVector.EMPTY:new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"timer","timer",-1266967739)], null));
return new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"db","db",993250759),cljs.core.assoc.call(null,db,new cljs.core.Keyword(null,"animate","animate",1850194573),cljs.core.not.call(null,animating)),new cljs.core.Keyword(null,"dispatch","dispatch",1319337009),disp], null);
}));
re_frame.core.reg_sub.call(null,new cljs.core.Keyword(null,"equations","equations",1884238648),(function (db,_){
return new cljs.core.Keyword(null,"equations","equations",1884238648).cljs$core$IFn$_invoke$arity$1(db);
}));
re_frame.core.reg_sub.call(null,new cljs.core.Keyword(null,"animate","animate",1850194573),(function (db,_){
return new cljs.core.Keyword(null,"animate","animate",1850194573).cljs$core$IFn$_invoke$arity$1(db);
}));
re_frame.core.reg_sub.call(null,new cljs.core.Keyword(null,"points","points",-1486596883),(function (db,_){
return new cljs.core.Keyword(null,"points","points",-1486596883).cljs$core$IFn$_invoke$arity$1(db);
}));
curve_fitting.canvas.toggle_animation_button = (function curve_fitting$canvas$toggle_animation_button(){
var animate_QMARK_ = cljs.core.deref.call(null,re_frame.core.subscribe.call(null,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"animate","animate",1850194573)], null)));
var text = (cljs.core.truth_(animate_QMARK_)?"Stop animation":"Start animation");
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"button","button",1456579943),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"class","class",-2030961996),"mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect",new cljs.core.Keyword(null,"on-click","on-click",1632826543),((function (animate_QMARK_,text){
return (function (){
return re_frame.core.dispatch.call(null,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"toggle-animation","toggle-animation",2103108973)], null));
});})(animate_QMARK_,text))
], null),text], null);
});
curve_fitting.canvas.add_equation_button = (function curve_fitting$canvas$add_equation_button(){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"button","button",1456579943),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"class","class",-2030961996),"mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect",new cljs.core.Keyword(null,"on-click","on-click",1632826543),(function (){
var a = ((2) - cljs.core.rand.call(null,(4)));
var b = ((1) + cljs.core.rand_int.call(null,(3)));
var c = ((5) - cljs.core.rand.call(null,(10)));
return re_frame.core.dispatch.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"new-eq","new-eq",-1545781695),((function (a,b,c){
return (function (x){
return ((a * Math.pow(x,b)) + c);
});})(a,b,c))
], null));
})], null),"Add equation"], null);
});
curve_fitting.canvas.plot_inner = (function curve_fitting$canvas$plot_inner(){
var graph = cljs.core.atom.call(null,null);
return reagent.core.create_class.call(null,new cljs.core.PersistentArrayMap(null, 4, [new cljs.core.Keyword(null,"reagent-render","reagent-render",-985383853),((function (graph){
return (function (){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"canvas#plot","canvas#plot",-705616260),new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"width","width",-384071477),"600",new cljs.core.Keyword(null,"height","height",1025178622),"600",new cljs.core.Keyword(null,"on-click","on-click",1632826543),((function (graph){
return (function (event){
return re_frame.core.dispatch.call(null,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"click","click",1912301393),curve_fitting.canvas.convert_scales.call(null,event,cljs.core.deref.call(null,graph))], null));
});})(graph))
], null)], null)], null);
});})(graph))
,new cljs.core.Keyword(null,"component-did-mount","component-did-mount",-1126910518),((function (graph){
return (function (comp){
var g = curve_fitting.canvas.make_graph.call(null);
return cljs.core.reset_BANG_.call(null,graph,g);
});})(graph))
,new cljs.core.Keyword(null,"component-did-update","component-did-update",-1468549173),((function (graph){
return (function (comp){
var map__1182 = reagent.core.props.call(null,comp);
var map__1182__$1 = ((((!((map__1182 == null)))?(((((map__1182.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__1182.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__1182):map__1182);
var equations = cljs.core.get.call(null,map__1182__$1,new cljs.core.Keyword(null,"equations","equations",1884238648));
var points = cljs.core.get.call(null,map__1182__$1,new cljs.core.Keyword(null,"points","points",-1486596883));
new cljs.core.Keyword(null,"context","context",-830191113).cljs$core$IFn$_invoke$arity$1(cljs.core.deref.call(null,graph)).clearRect(new cljs.core.Keyword(null,"min-x","min-x",-1544012261).cljs$core$IFn$_invoke$arity$1(cljs.core.deref.call(null,graph)),new cljs.core.Keyword(null,"min-y","min-y",-1969872948).cljs$core$IFn$_invoke$arity$1(cljs.core.deref.call(null,graph)),new cljs.core.Keyword(null,"range-x","range-x",-1724389287).cljs$core$IFn$_invoke$arity$1(cljs.core.deref.call(null,graph)),new cljs.core.Keyword(null,"range-y","range-y",-541153491).cljs$core$IFn$_invoke$arity$1(cljs.core.deref.call(null,graph)));

cljs.core.run_BANG_.call(null,((function (map__1182,map__1182__$1,equations,points,graph){
return (function (p1__1180_SHARP_){
return curve_fitting.canvas.add_equation_BANG_.call(null,cljs.core.deref.call(null,graph),new cljs.core.Keyword(null,"equation","equation",-499527745).cljs$core$IFn$_invoke$arity$1(p1__1180_SHARP_),new cljs.core.Keyword(null,"opacity","opacity",397153780).cljs$core$IFn$_invoke$arity$1(p1__1180_SHARP_));
});})(map__1182,map__1182__$1,equations,points,graph))
,equations);

return cljs.core.run_BANG_.call(null,((function (map__1182,map__1182__$1,equations,points,graph){
return (function (p1__1181_SHARP_){
return curve_fitting.canvas.add_point_BANG_.call(null,cljs.core.deref.call(null,graph),p1__1181_SHARP_);
});})(map__1182,map__1182__$1,equations,points,graph))
,points);
});})(graph))
,new cljs.core.Keyword(null,"display-name","display-name",694513143),"plot-inner"], null));
});
curve_fitting.canvas.plot_outer = (function curve_fitting$canvas$plot_outer(){
var equations = re_frame.core.subscribe.call(null,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"equations","equations",1884238648)], null));
var points = re_frame.core.subscribe.call(null,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"points","points",-1486596883)], null));
return ((function (equations,points){
return (function (){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [curve_fitting.canvas.plot_inner,new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"equations","equations",1884238648),cljs.core.deref.call(null,equations),new cljs.core.Keyword(null,"points","points",-1486596883),cljs.core.deref.call(null,points)], null)], null);
});
;})(equations,points))
});
curve_fitting.canvas.ui = (function curve_fitting$canvas$ui(){
return new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"main","main",-2117802661),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"mdl-layout__content"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"mdl-grid"], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"class","class",-2030961996),"mdl-cell--12-col"], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [curve_fitting.canvas.add_equation_button], null),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [curve_fitting.canvas.toggle_animation_button], null)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"style","style",-496642736),new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"padding","padding",1660304693),"16px"], null)], null),new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [curve_fitting.canvas.plot_outer], null)], null)], null)], null)], null);
});
curve_fitting.canvas.run = (function curve_fitting$canvas$run(){
re_frame.core.dispatch_sync.call(null,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"initialize","initialize",609952913)], null));

return reagent.core.render.call(null,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [curve_fitting.canvas.ui], null),document.getElementById("app"));
});
goog.exportSymbol('curve_fitting.canvas.run', curve_fitting.canvas.run);
curve_fitting.canvas.run.call(null);

//# sourceMappingURL=canvas.js.map
