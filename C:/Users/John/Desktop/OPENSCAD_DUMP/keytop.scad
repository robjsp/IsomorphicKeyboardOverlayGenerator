include<values.scad>;
keytop();

module keytopShape(){
polyhedron(Points,Faces);
}
module keytop(){
difference(){
keytopShape();
translate([(b+c)/4,(d+a)/4,-0.1])
scale(holeScale,holeScale,holeScale)
keytopShape();
edge(6,11,1,-1);
edge(6,7,1,1);
edge(7,8,1,1);
edge(8,9,1,1);
edge(9,10,1,-1,-1);
edge(10,11,1,-1);
}
module edge (point1,point2,d1,d2,d3=1){
difference(){
translate([Points[point1][0],Points[point1][1],Points[point1][2]])
rotate([0,90,atan((Points[point2][1]-Points[point1][1])/(Points[point2][0]-Points[point1][0]))])
translate([0,0,d3*30])
rotate(45,0,0)
cube([pow(2*(edgeRadius*edgeRadius),1/2),pow(2*(edgeRadius*edgeRadius),1/2),90],true);
translate([Points[point1][0],Points[point1][1],Points[point1][2]])
rotate([0,90,atan((Points[point2][1]-Points[point1][1])/(Points[point2][0]-Points[point1][0]))])
translate([d1*edgeRadius,d2*edgeRadius,d3*30])
cylinder(90,edgeRadius,edgeRadius, true);
}
}

}
