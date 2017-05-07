/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.canting;

import com.jbatik.filetype.cor.CorakDataObject;
import com.jbatik.lsystem.turtle.BaseVector;
import com.jbatik.lsystem.turtle.Surface;
import com.jbatik.lsystem.turtle.Tube;
import com.jbatik.lsystem.util.VectorUtil;
import com.jbatik.modules.corak.CorakLSystem;
import com.jbatik.modules.corak.appearance.SolidAppearance;
import com.jbatik.modules.corak.appearance.TextureAppearance;
import com.jbatik.modules.corak.appearance.WireframeAppearance;
import com.jbatik.modules.corak.io.CorakSerializer;
import com.jbatik.modules.corak.node.CorakFileUtil;
import com.jbatik.modules.interfaces.Appearance3DChangerCookie;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleFanArray;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * A Canting object behaves more or less like "the turtle" in L-System. It draws
 * objects which will finally be rendered in our canvas in CorakViewElement.
 *
 * @author RAPID01
 */
public class Canting {

    private final CorakLSystem origCor;
    private final CorakDataObject origCorDataObject;
    private CorakLSystem currentCor;
    private double currentLength;
    private double currentAngle;
    private double currentWidth;
    private BranchGroup batikBG;
    private TransformGroup batikTG;

    private final HashMap<CorakLSystem, CorakStructure> structures;

    private CorakStructure currStructure;

    //canting position and directions
    private final BaseVector bv;

    //state variables
    private int currentColorIdx = 1; //starts from 1, better UX in the GUI
    private boolean valued = false;
    private Double customValue = null; //e.g. 30 in +(30)F 
    private String externalCor = ""; //e.g. daun.cor in A(daun.cor)+F
    private boolean skipExtCor = false;
    private boolean skipAll = false;
    private boolean polygonMode = false;

    private Surface currentSurface = new Tube();
    private String currentSurfaceName;
    private final HashMap<String, CorakLSystem> loadedCor = new HashMap<>();
    private final HashMap<String, BranchGroup> loadedObj = new HashMap<>();
    private ArrayList<String> tempSurfaceNotExists = new ArrayList<>();

    //temporary variables
    private Point3d[] prevTopPoints = null;
    private LinkedList<Point3d> currentVertexes;
    private String lastIgnored = "";

    //stacks for branching
    private LinkedList<BaseVector> branchingPoints = new LinkedList<>();
    private LinkedList<Point3d[]> topPoints = new LinkedList<>();
    private LinkedList<Double> widths = new LinkedList<>();
    private LinkedList<Double> angles = new LinkedList<>();
    private LinkedList<Double> lengths = new LinkedList<>();
    private LinkedList<Integer> colorIdxes = new LinkedList<>();
    private LinkedList<F> prevFs = new LinkedList<>();

    //global variable for rendering options
    private boolean encloseOBJ;

    public Canting(CorakLSystem cor, CorakDataObject obj) {

        bv = new BaseVector();

        ArrayList<Color3f> colorList = new ArrayList<>();
        colorList.add(0, null);
//        colorList.add(1, new Color3f(0.0f, 0.5f, 0.0f)); //default color: dark green
//        colorList.add(2, new Color3f(0.5f, 0.0f, 0.0f));
//        colorList.add(3, new Color3f(0.0f, 0.5f, 0.5f));
//        colorList.addAll(cor.getColor3fs()); // convert dipakai disini
        colorList.addAll(cor.getColor3fs());

        ArrayList<String> textureList = new ArrayList<>();
        textureList.add(0, null);
//        textureList.add(1, "putih.jpg");
//        textureList.add(2, "biru.jpg");
//        textureList.add(3, "hijau.jpg");
        textureList.addAll(cor.getTextures());

        this.origCor = cor;
        this.currentCor = cor;
        currentLength = cor.getLength();
        currentAngle = cor.getAngle();
        currentWidth = cor.getWidth();
        this.origCorDataObject = obj;

        structures = new HashMap<>();
        structures.put(origCor, new CorakStructure(colorList, textureList));
        currStructure = structures.get(cor);
    }

    List<String> missingExternal = new ArrayList<>();

    /**
     * Main access to generate() method, using Axiom and initial iteration as
     * params. After the recursion ends, all shapes will be generated. Then
     * attach the generated shapes to mainTG.
     */
    public void generate(boolean isForOBJ) {

        this.encloseOBJ = isForOBJ;

        batikBG = new BranchGroup();
        batikBG.setCapability(BranchGroup.ALLOW_DETACH);
        batikBG.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
        batikBG.setCapability(BranchGroup.ALLOW_BOUNDS_WRITE);
        batikBG.setCapability(BranchGroup.ALLOW_AUTO_COMPUTE_BOUNDS_READ);
        batikBG.setCapability(BranchGroup.ALLOW_AUTO_COMPUTE_BOUNDS_WRITE);
        batikTG = new TransformGroup();
        batikTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        batikTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        batikTG.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        batikTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        batikTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        batikTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        batikTG.setCapability(TransformGroup.ALLOW_BOUNDS_READ);
        batikBG.addChild(batikTG);
        batikBG.setBoundsAutoCompute(true);

        missingExternal.clear();
        prevF = null;
        if (!currentCor.isInvalid()) {
            generate(currentCor.getAxiom(), currentCor.getIteration());
            castUncastedF();
            closeUnclosedPolygon();

            structures.entrySet().stream().map((e) -> e.getValue()).forEach((struct) -> {
                ArrayList<Color3f> colorList = struct.getColorList();
                int colorListSize = colorList.size();
                for (int idx = 1; idx < colorListSize; idx++) {
                    ShapeInfo shapeInfo = struct.getShapeInfoByColor(idx);
                    Color3f color = colorList.get(idx);
                    //create F
                    if (shapeInfo.getFPoints().size() > 0) {
                        Point3d[] fPoints = shapeInfo.getFPointsInArray();
                        Color3f[] colors = new Color3f[fPoints.length];
                        Arrays.fill(colors, new Color3f(color));

                        QuadArray quadArray = new QuadArray(
                                fPoints.length,
                                QuadArray.COORDINATES
                                | QuadArray.COLOR_3
                                | QuadArray.NORMALS
                                | QuadArray.TEXTURE_COORDINATE_2
                        );
                        quadArray.setCoordinates(0, fPoints);
                        quadArray.setNormals(0, shapeInfo.getFNormalsInArray());
                        quadArray.setTextureCoordinates(0, 0, shapeInfo.getFTexCoordsInArray());
                        quadArray.setColors(0, colors);

                        Shape3D F = new Shape3D();
                        F.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
                        F.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
                        F.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_READ);
                        F.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE);
                        F.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
                        F.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

                        //form the shape using the quad array
                        F.setGeometry(quadArray);

                        WireframeAppearance wireAprProvider = new WireframeAppearance();
                        F.setAppearance(wireAprProvider.getAppearance());

                        batikTG.addChild(F);
                        shapeInfo.setFShape(F);
                    }

                    //create polygon
                    if (shapeInfo.getPolygonPoints().size() > 0) {
                        Point3d[] polygonPoints = shapeInfo.getPolygonPointsInArray();

                        TriangleFanArray tfArray = new TriangleFanArray(
                                polygonPoints.length,
                                TriangleFanArray.COORDINATES
                                | TriangleFanArray.COLOR_3
                                | TriangleFanArray.TEXTURE_COORDINATE_2,
                                shapeInfo.getPolygonStripVertexCountsInArray()
                        );

                        Color3f[] colorsForPolygon = new Color3f[polygonPoints.length];
                        Arrays.fill(colorsForPolygon, color);

                        tfArray.setCoordinates(0, polygonPoints);
                        tfArray.setTextureCoordinates(0, 0, shapeInfo.getPolygonTexCoordsInArray());
                        tfArray.setColors(0, colorsForPolygon);

                        //generate normals automatically
                        GeometryInfo gi = new GeometryInfo(tfArray);
                        NormalGenerator normalGenerator = new NormalGenerator(0);
                        normalGenerator.generateNormals(gi);

                        Shape3D polygon = new Shape3D();
                        polygon.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
                        polygon.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
                        polygon.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_READ);
                        polygon.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE);
                        polygon.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
                        polygon.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

                        //set geometry with generated normals
                        polygon.setGeometry(gi.getGeometryArray());

                        WireframeAppearance wireAprProvider = new WireframeAppearance();
                        polygon.setAppearance(wireAprProvider.getAppearance());

                        batikTG.addChild(polygon);
                        shapeInfo.setPolygonShape(polygon);
                    }

                    //create enclose, if necessary
                    if (shapeInfo.getEnclosePoints().size() > 0) {
                        Point3d[] enclosePoints = shapeInfo.getEnclosePointsInArray();

                        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);

                        //set coordinates
                        gi.setCoordinates(enclosePoints);
                        gi.setStripCounts(shapeInfo.getEncloseStripVertexCountsInArray());

                        Color3f[] colorsForEnclose = new Color3f[enclosePoints.length];
                        Arrays.fill(colorsForEnclose, color);

                        //set colors
                        gi.setColors(colorsForEnclose);

                        //TODO : set Texture Coordinate?
                        //generate normal automagically
                        NormalGenerator ng = new NormalGenerator();
                        ng.generateNormals(gi);

                        //generate strips automagically
                        Stripifier st = new Stripifier();
                        st.stripify(gi);

                        Shape3D enclose = new Shape3D();
                        WireframeAppearance wireAprProvider = new WireframeAppearance();
                        enclose.setAppearance(wireAprProvider.getAppearance());
                        enclose.setGeometry(gi.getGeometryArray());
                        
                        enclose.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
                        enclose.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
                        enclose.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_READ);
                        enclose.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE);

                        batikTG.addChild(enclose);
                        shapeInfo.setEnclosingShape(enclose);
                    }

                    struct.setShapeInfoByColor(idx, shapeInfo);

                }
            });
        }
    }

    /**
     * Reads the whole axiom and rules as it generates the shapes at the same
     * time. At the end of each shape generation, this method adds the shape to
     * our pool of shapes -> colorShapeMap.
     *
     * @param cmds
     * @param iter
     */
    private void generate(String cmds, int iter) {
        for (int i = 0; i < cmds.length(); i++) {
            char curSymbol = cmds.charAt(i);
            String detail = currentCor.getRules().get(curSymbol);

            if (detail != null && iter > 0) {

                generate(detail, iter - 1);

            } else {
                switch (curSymbol) {
                    /**
                     * TODO : gimana ini caranya biar list of terminal symbols
                     * ini bisa expanded/overriden by another module perlu
                     * konsul ke yg ngerti design Netbeans Platform :| sementara
                     * gini aja dulu
                     */
                    case '#': {
                        log("start comment");
                        i = cmds.length();
                        log("end of comment");
                        break;
                    }
                    case '*': {
                        log("generate(external .cor file)");
                        i = parseValue(cmds, i);
                        if (externalCor.toLowerCase().endsWith(".obj")) {
                            generateObj(externalCor);
                        } else if (externalCor.toLowerCase().endsWith(".cor") && !skipExtCor) {
                            generateCor(externalCor);
                        }
                        break;
                    }
                    case 'F': {
                        log("draw F");
                        i = drawG(cmds, i);
                        break;
                    }
                    case 'f': {
                        log("move, record vertex");
                        i = move(cmds, i);
                        recordVertex();
                        break;
                    }
                    case 'Z': {
                        log("draw Z");
                        double defaultLength = currentLength;
                        double defaultWidth = currentWidth;
                        currentLength = defaultLength / 2;
                        currentWidth = defaultWidth / 2;
                        i = drawG(cmds, i);
                        currentLength = defaultLength;
                        currentWidth = defaultWidth;
                        break;
                    }
                    case 'z': {
                        log("move halfway, record vertex");
                        double defaultLength = currentLength;
                        currentLength = defaultLength / 2;
                        i = move(cmds, i);
                        recordVertex();
                        currentLength = defaultLength;
                        break;
                    }
                    case 'c': {
                        log("change color");
                        i = setColor(cmds, i);
                        break;
                    }
                    case 'g': {
                        log("move");
                        i = move(cmds, i);
                        break;
                    }
                    case '.': {
                        log("record vertex");
                        recordVertex();
                        break;
                    }
                    case '+': {
                        log("turn right");
                        i = turn(cmds, i, true); //true = turn right
                        break;
                    }
                    case '-': {
                        log("turn left");
                        i = turn(cmds, i, false); //false = turn left
                        break;
                    }
                    case '^': {
                        log("turn inside canvas");
                        i = pitch(cmds, i, true); //true = pitch up
                        break;
                    }
                    case '&': {
                        log("turn outside canvas");
                        i = pitch(cmds, i, false); //false = pitch down
                        break;
                    }
                    case '>': {
                        log("roll clockwise");
                        i = roll(cmds, i, true); //true = roll clockwise
                        break;
                    }
                    case '<': {
                        log("roll counter-clockwise");
                        i = roll(cmds, i, false); //false = roll counter-clockwise
                        break;
                    }
                    case '%': {
                        log("roll 180 degrees");
                        double defaultAngle = currentAngle;
                        currentAngle = 180.0;
                        i = roll(cmds, i, true);
                        currentAngle = defaultAngle;
                        break;
                    }
                    case '|': {
                        log("turn 180 degrees");
                        double defaultAngle = currentAngle;
                        currentAngle = 180.0;
                        i = turn(cmds, i, true);
                        currentAngle = defaultAngle;
                        break;
                    }
                    case '$': {
                        log("roll horizon");
                        i = rollHorizon(cmds, i);
                        break;
                    }
                    case '~':
                        log("reflection");
                        break;
                    case '{': {
                        log("start draw polygon");
                        startPolygon();
                        break;
                    }
                    case '}': {
                        log("end draw polygon");
                        endPolygon();
                        break;
                    }
                    case '[': {
                        log("start branch");
                        startBranch();
                        break;
                    }
                    case ']': {
                        log("end branch");
                        endBranch();
                        break;
                    }
                    case '\'': {
                        log("decrease length");
                        i = decLength(cmds, i);
                        break;
                    }
                    case '"': {
                        log("increase length");
                        i = incLength(cmds, i);
                        break;
                    }
                    case '!': {
                        log("decrease thickness");
                        i = decThickness(cmds, i);
                        break;
                    }
                    case '?': {
                        log("increase thickness");
                        i = incThickness(cmds, i);
                        break;
                    }
                    case ':': {
                        log("increase angle");
                        i = decAngle(cmds, i);
                        break;
                    }
                    case ';': {
                        log("increase angle");
                        i = incAngle(cmds, i);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    /**
     * First seek on previously loaded obj library. Create new and add to
     * library if no suitable BranchGroup is found.
     *
     * @param objPath
     */
    private void generateObj(String objPath) {
        if (missingExternal.contains(objPath)) {
            return;
        }
        BranchGroup BG = loadedObj.get(objPath);
        if (BG == null) {
            //try create
            BranchGroup newBG = OBJLoader.createBranchGroup(objPath);
            if (newBG == null) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                        "OBJ File parse error : " + objPath,
                        "Notice",
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.WARNING_MESSAGE));
                missingExternal.add(objPath);
                return;
            }

            TransformGroup tgcDummy = new TransformGroup();
            tgcDummy.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            tgcDummy.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            tgcDummy.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

            BranchGroup bgcDummy = new BranchGroup();
            bgcDummy.addChild(tgcDummy);
            tgcDummy.addChild(newBG);

            BranchGroup BGN = new BranchGroup();
            BGN.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
            BGN.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
            BGN.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
            BGN.setCapability(BranchGroup.ALLOW_DETACH);
            BGN.addChild(bgcDummy);
            // and add to lib
            loadedObj.put(objPath, BGN);

            BG = BGN;

        }
        BranchGroup cloneBG = (BranchGroup) BG.cloneTree();

        Transform3D trans = new Transform3D();
        Matrix3d MatRot = new Matrix3d();
        MatRot.set(bv.getMatRot());
        Vector3d VecTrans = new Vector3d();
        VecTrans.set(bv.center);
        trans.set(MatRot, VecTrans, 1);

        TransformGroup localTG
                = (TransformGroup) ((BranchGroup) cloneBG.getChild(0)).getChild(0);

        localTG.setTransform(trans);
        localTG.clearCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        localTG.clearCapability(TransformGroup.ENABLE_PICK_REPORTING);

        //add shape3D's to shapes
//        this.shapes.addAll(getActiveShape3D(cloneBG));
        BranchGroup x = (BranchGroup) cloneBG.getChild(0);
        x.detach();
        batikTG.addChild(x);

    }

    //stacks for polygon
    private LinkedList<LinkedList<Point3d>> vertexes = new LinkedList<>();

    private void closeUnclosedPolygon() {
        while (!vertexes.isEmpty()) {
            endPolygon();
        }
    }

    private class F {

        Point3d[] bot;
        Point3d botCenter;//kepake buat bikin normal saat assign quad
        Point3d[] top;
        Point3d topCenter;//kepake buat bikin normal saat assign quad
        CorakLSystem corak;
        int realColorIndex;
        Vector3d topNormal;//bv.dir saat membuat top
        private LinkedList<Point3d> currentFPoints;
        private LinkedList<Vector3f> currentFNormals;
        private LinkedList<TexCoord2f> currentFTexCoords;
        boolean isWaitingForBranch = false;//menunggu branch yang memuat dirinya ditutup
        boolean isDependent = false;//menunggu F to which it depends being casted
        private boolean isHead;//this.bot is not dependent to any other F
        private boolean isTail;//this.top should be closed (for export to OBJ only)
        private boolean casted = false;

        private F(CorakLSystem currentCor, int idx, Point3d[] bot, Point3d c) {
            this.realColorIndex = idx;
            this.corak = currentCor;
            this.bot = bot;
            this.botCenter = c;
            this.isHead = false;
            this.isTail = false;
        }

        void assignTopCandidate(Point3d[] t, Point3d c, Vector3d d) {
            assert t.length == this.bot.length;
            this.top = t;
            this.topCenter = new Point3d(c);
            this.topNormal = new Vector3d(d);
        }

        private void castLast() {
            if (encloseOBJ) {
                //set flag agar tutup top
                this.isTail = true;
            }
            cast();
        }

        /**
         * Register bot and top coordinates into corresponding shapeInfo.
         */
        private void cast() {
            if (!isWaitingForBranch && !isDependent) {
                assert casted == false;
                int pointsnum = top.length;
                //new set of points (new tube)
                CorakStructure struct = structures.get(this.corak);
                ShapeInfo shapeInfo = struct.getShapeInfoByColor(realColorIndex);
                currentFPoints = shapeInfo.getFPoints();
                currentFNormals = shapeInfo.getFNormals();
                currentFTexCoords = shapeInfo.getFTexCoords();
                for (int j = 0, k = 0; j < pointsnum * 4; j += 4, k++) {
                    Point3d nextBotPts = bot[(k + 1) % pointsnum];
                    Point3d nextTopPts = top[(k + 1) % pointsnum];

                    assignToQuad(bot[k], botCenter, new TexCoord2f(0.0f, 0.0f));

                    assignToQuad(nextBotPts, botCenter, new TexCoord2f(1.0f, 0.0f));

                    assignToQuad(nextTopPts, topCenter, new TexCoord2f(1.0f, 1.0f));

                    assignToQuad(top[k], topCenter, new TexCoord2f(0.0f, 1.0f));

                }
                if (encloseOBJ) {
                    List enclosePoints = shapeInfo.getEnclosePoints();
                    if (isHead) {
                        //tutup bawah, urutan diputar biar counterclockwise 
                        //apa biar clockwise ya begitulah pokoknya harus diputar
                        //soalnya kalo ga diputar, bolong kalo diliat dari bawah
                        //ujung2nya harus di set force2sided di 3dxmax
                        for (int x = bot.length - 1; x >= 0; x--) {
                            enclosePoints.add(bot[x]);
                        }
                        shapeInfo.addEncloseStripVertexCount(bot.length);
                    }
                    if (isTail) {
                        //tutup atas
                        enclosePoints.addAll(Arrays.asList(top));
                        shapeInfo.addEncloseStripVertexCount(top.length);
                    }
                }

                if (!dependentF.isEmpty()) {
                    resolveDependants();
                }
                casted = true;

            }
        }

        private void assignToQuad(Point3d point, Point3d bvCenter, TexCoord2f texCorner) {
            currentFPoints.add(point);

            Vector3d tempNormal = new Vector3d();
            tempNormal.sub(point, bvCenter);
            currentFNormals.add(new Vector3f(tempNormal));
            currentFTexCoords.add(texCorner);
        }

        private final LinkedList<F> dependentF = new LinkedList<>();

        private void addDependentF(F drawnF) {
            dependentF.add(drawnF);
        }

        /**
         * Set bottom points of all F in the dependant list with this.top, then
         * cast them.
         */
        private void resolveDependants() {
            while (!dependentF.isEmpty()) {
                F f = dependentF.pop();
                f.bot = this.top;
                f.isDependent = false;
                f.cast();
            }
        }
    }

    /**
     * To generate shapes of external .cor file as in *(daun.cor). This method
     * put the original cor down, draw another cor, take the original cor again.
     *
     * @param externalCorPath
     */
    private void generateCor(String externalCorPath) {
        if (missingExternal.contains(externalCorPath)) {
            return;
        }
        //simpan kondisi mula-mula
        CorakLSystem lastCor = currentCor;
        double lastAngle = currentAngle;
        double lastWidth = currentWidth;
        double lastLength = currentLength;
        int lastColorIdx = currentColorIdx;
        Surface lastSurface = currentSurface;
        BaseVector lastBV = new BaseVector(bv);
        F lastF = prevF;
        boolean isPoly = polygonMode;

        //load kondisi corak external
        //pertama-tama cek dari loadedCor
        CorakLSystem candidate = loadedCor.get(externalCorPath);
        if (candidate != null) {
            currentCor = candidate;
        } else {
            //belum pernah di load sebelumnya
            CorakLSystem newCor = CorakSerializer.deserialize(externalCorPath);
            currentCor = newCor;
            loadedCor.put(externalCorPath, newCor);
        }

        ArrayList<Color3f> extColorList = new ArrayList<>();
        extColorList.add(0, null);
        extColorList.addAll(currentCor.getColor3fs());

        ArrayList<String> extTexList = new ArrayList<>();
        extTexList.add(0, null);
        extTexList.addAll(currentCor.getTextures());

        if (candidate == null) {
            //belum pernah di load sebelumnya
            structures.put(currentCor, new CorakStructure(extColorList, extTexList));
        }
        //init variables for drawing
        currentAngle = currentCor.getAngle();
        currentWidth = currentCor.getWidth();
        currentLength = currentCor.getLength();
        currentColorIdx = 1;
        currentSurface = new Tube();
        currStructure = structures.get(currentCor);
        prevF = null;//always start new cor with null F
        polygonMode = false;
        currentVertexes = null;//always start new cor with null

        //temporary variables for branching & polygon
        LinkedList bvs = branchingPoints;
        LinkedList cidx = colorIdxes;
        LinkedList w = widths;
        LinkedList a = angles;
        LinkedList le = lengths;
        LinkedList tp = topPoints;
        LinkedList f = prevFs;
        LinkedList polyv = vertexes;

        //reset all branching stack for external cor usage
        branchingPoints = new LinkedList<>();
        colorIdxes = new LinkedList<>();
        widths = new LinkedList<>();
        angles = new LinkedList<>();
        lengths = new LinkedList<>();
        topPoints = new LinkedList<>();
        prevFs = new LinkedList<>();
        vertexes = new LinkedList<>();

        //generating external cor
        if (!currentCor.isInvalid()) {
            generate(currentCor.getAxiom(), currentCor.getIteration());
            castUncastedF();
            closeUnclosedPolygon();
        }

        //load stacks for branching back
        vertexes = polyv;
        prevFs = f;
        topPoints = tp;
        lengths = le;
        angles = a;
        widths = w;
        colorIdxes = cidx;
        branchingPoints = bvs;

        //load last states
        polygonMode = isPoly;
        prevF = lastF;
        bv.set(lastBV);
        currentSurface = lastSurface;
        currentColorIdx = lastColorIdx;
        currentLength = lastLength;
        currentWidth = lastWidth;
        currentAngle = lastAngle;
        currentCor = lastCor;

        currStructure = structures.get(currentCor);
    }

    public BranchGroup getBatikBG() {
        return batikBG;
    }

    /**
     * fills customValue or externalCor
     *
     * @param currentCmds
     * @param i
     * @return new i after parsing the value
     */
    private int parseValue(String currentCmds, int i) {

        skipExtCor = false;

        String value = "";

        valued = false;
        if (i + 1 < currentCmds.length()) {
            if (currentCmds.charAt(i + 1) == '(') {
                i += 2;
                while (currentCmds.charAt(i) != ')') {
                    value = value + currentCmds.charAt(i);
                    i++;
                }
                try {
                    customValue = Double.parseDouble(value);
                    log("value= " + customValue);
                } catch (NumberFormatException e) {
                    //not a float, must be name of external .cor file
                    String externalCorPath = getCorFilePath(value);
                    File externalCorFile = new File(externalCorPath);
                    if (!missingExternal.contains(externalCorFile.getAbsolutePath())) {
                        if (!externalCorFile.exists()) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                                    "File can't be found: " + externalCorFile.getAbsolutePath(),
                                    "Notice",
                                    NotifyDescriptor.DEFAULT_OPTION,
                                    NotifyDescriptor.WARNING_MESSAGE));
                            missingExternal.add(externalCorFile.getAbsolutePath());
                        } else {
                            if (value.toLowerCase().endsWith(".cor")) {

                                //if there's cyclic reference
                                if (externalCorFile.getName().equals(origCorDataObject.getPrimaryFile().getNameExt())) {
                                    if (!lastIgnored.equals(externalCorFile.getName())) {
                                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                                                "The symbol \n*(" + origCorDataObject.getPrimaryFile().getNameExt() + ")\nis ignored because of cyclic reference.\nTry not to load a Corak within itself.",
                                                "Notice",
                                                NotifyDescriptor.DEFAULT_OPTION,
                                                NotifyDescriptor.WARNING_MESSAGE));
                                    }
                                    skipExtCor = true;
                                    lastIgnored = externalCorFile.getName();
                                }

                                if (!skipAll) {
                                    externalCor = externalCorFile.getAbsolutePath();
                                }

                                //test apakah ini valid path apa engga
                                if (CorakSerializer.deserialize(externalCorFile.getAbsolutePath()) == null) {
                                    skipExtCor = true;
                                    skipAll = true;
                                    missingExternal.add(externalCorFile.getAbsolutePath());
                                }

                                log("path= " + externalCorPath);
                            } else if (value.toLowerCase().endsWith(".obj")) {
                                externalCor = externalCorFile.getAbsolutePath();
                            }
                        }
                    }
                }

                if (customValue != null) {
                    valued = true;
                }
            }
        }

        //now index of "for" has jumped forward to ')'
        return i;
    }

    private int parseFUnit(String currentCmds, int i) {

        String value = "";

        valued = false;
        if (i + 1 < currentCmds.length()) {
            if (currentCmds.charAt(i + 1) == '(') {
                i += 2;
                while (currentCmds.charAt(i) != ')') {
                    value = value + currentCmds.charAt(i);
                    i++;
                }
                if (value.length() > 0) {
                    valued = true;
                    if ((currentSurfaceName == null) || (currentSurfaceName != null && !currentSurfaceName.equals(value))) {
                        currentSurfaceName = value;
                    }
                }
            }
        }

        //now index of "for" has jumped forward to ')'
        return i;
    }

    private F prevF;

    private void castUncastedF() {
        if (prevF != null) {
            prevF.castLast();
        }
        while (!prevFs.isEmpty()) {
            //take care of unclosed branches
            F uncasted = prevFs.pop();
            if (uncasted != null) {
                uncasted.isWaitingForBranch = false;
                uncasted.castLast();
            }
        }
    }

    /**
     * Semacam drawF tapi pakai penundaan dalam invoke assignToQuadArray
     *
     * @param c
     * @param i
     * @return
     */
    private int drawG(String currentCmds, int i) {
        //get the actual color index to be used
        int tempColorIdx;
        int n = (currStructure.getColorList().size() - 1);
        if (n == 0) {
            tempColorIdx = 0;
        } else {
            tempColorIdx = currentColorIdx % n;
            if (tempColorIdx == 0) {
                tempColorIdx = currStructure.getColorList().size() - 1;
            }
        }

        i = parseFUnit(currentCmds, i); // currentSurfaceName <-- name inside the brackets
        boolean surfaceFound = false;

        if (valued) {
            //F(something)
            List<Surface> surfaceList = currentCor.getSurfaces();
            for (Surface surface : surfaceList) {
                if (surface.getName().equals(currentSurfaceName)) {
                    surfaceFound = true;
                    currentSurface = surface;
                    break;
                }
            }

            if (!surfaceFound
                    && !tempSurfaceNotExists.contains(currentSurfaceName)) { //prevents error popup shown multiple times
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                        "FUnit (" + currentSurfaceName + ") is not exists.\nUsing previous FUnit instead.",
                        "Notice",
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.WARNING_MESSAGE));
                tempSurfaceNotExists.add(currentSurfaceName);
            }
        }

        //now we know the number of points in currentSurface,
        //check again with previous top points, connect them again if they have
        //the same number of points
        F drawnF;
        float r = (float) currentWidth;
        if (prevF == null) {
            //baru mulai gambar (lagi)
            //ini bisa terjadi either karena F yang mau digambar adalah F pertama di corak itu
            //atau karena move (f/z kecil)
            drawnF = new F(currentCor, tempColorIdx, bv.vRound(r, currentSurface), bv.center);
            drawnF.isHead = true;
        } else {
            //berarti prevF sudah ada
            //kita cek apakah kita harus nempel apa engga sama prevF ini
            //nempel itu DILAKUKAN JIKA DAN HANYA JIKA
            //prevF dan F yang akan digambar ini berasal dari corak yang sama 
            //DAN color index yang sama
            //DAN banyak titik surface yang sama
            if ((currentCor == prevF.corak)//TODO : cek lagi ini perlu apa engga
                    && (tempColorIdx == prevF.realColorIndex)
                    && (currentSurface.getPoints().length == prevF.top.length)) {
                //berarti nempel
                //nempel ini, kita lihat turning angle nya berapa
                //dengan bv belum maju, tapi sudah belok sebesar turning angle,
                //toppoint bakal kita rotate around an axis, yang mana axis ini 
                //ada di bidang awalnya top
                //di sini bidangnya terdefinisi karena kita simpan F.topCenter dan F.topNormal
                //besarnya rotate sendiri adalah sebesar setengah dari sudut antara
                //topNormal dengan bv.dir saat ini
                //KECUALI
                //kalau prevF dan F yang baru ini beda cabang
                if (prevF == prevFs.peekFirst()) {
                    //artinya baru memulai cabang, tapi ga geser color index
                    //di kasus ini, bot dari F di cabang akan nempel top prevF
                    //tanpa tilting top prevF
                    drawnF = new F(currentCor, tempColorIdx, prevF.top, bv.center);
                    //tapi si drawnF ini can't be casted before the top points of prevF are fixed
                    //artinya ada dependency antara si drawnF ini dan prevF
                    drawnF.isDependent = true;
                    prevF.addDependentF(drawnF);
                } else {
                    double tiltingAngle = bv.dir.angle(prevF.topNormal) / 2;
                    if (tiltingAngle != 0) {
                        Vector3d rotAxis = new Vector3d();
                        rotAxis.cross(prevF.topNormal, bv.dir);
                        Transform3D tiltingMatrix = VectorUtil.rotAroundAxis(bv.center, rotAxis, tiltingAngle);
                        Point3d[] tiltedTop = new Point3d[prevF.top.length];
                        for (int id = 0; id < prevF.top.length; id++) {
                            Point3d tiltedPoint = new Point3d(prevF.top[id]);
                            tiltingMatrix.transform(tiltedPoint);
                            tiltedTop[id] = tiltedPoint;
                        }
                        //sudah di tilt, prevF sudah bisa assign quad array 
                        //menggunakan prevF.bot dan tiltedTop
                        prevF.top = tiltedTop;
                    }
                    prevF.cast();
                    //mulai bikin yang baru
                    drawnF = new F(currentCor, tempColorIdx, prevF.top, bv.center);
                }
            } else {
                //berarti ga nempel, prevF langsung diassign saja (kalo bisa, alias ga lagi waiting)
                //bisa terjadi karena ganti color index
                //atau karena ganti surface
                prevF.castLast();
                //akibatnya bot dari yang baru ini tidak ngambil dari top sebelumnya
                drawnF = new F(currentCor, tempColorIdx, bv.vRound(r, currentSurface), bv.center);
                drawnF.isHead = true;
            }
        }
        //di titik ini kita sudah beres cast previous F (if any) dan
        //menentukan bot points dari F yang baru akan digambar
        bv.Move(currentLength);
        drawnF.assignTopCandidate(bv.vRound(r, currentSurface), bv.center, bv.dir);
        prevF = drawnF;
        return i;
    }

    private void startPolygon() {
        polygonMode = true;
        if (currentVertexes != null) {
            //poly dalam poly
            vertexes.push(new LinkedList<>(currentVertexes));
        }
        currentVertexes = new LinkedList<>();
        recordVertex();

    }

    //vertex for polygon
    private void recordVertex() {
        if (polygonMode) {
            currentVertexes.add(new Point3d(bv.center));
        }
    }
    private static final TexCoord2f BL_COORD = new TexCoord2f(0.0f, 0.0f);
    private static final TexCoord2f BR_COORD = new TexCoord2f(1.0f, 0.0f);
    private static final TexCoord2f TR_COORD = new TexCoord2f(1.0f, 1.0f);
    private static final TexCoord2f TL_COORD = new TexCoord2f(0.0f, 1.0f);

    private void endPolygon() {
        if (currentVertexes != null && currentVertexes.size() >= 3) {
            //note that currentVertexes may be null iff startPolygon is never called IN CURRENT COR
            int stripSize = currentVertexes.size();

            //finish this polygon
            int realUsedColorIdx;
            int n = (currStructure.getColorList().size() - 1);
            if (n == 0) {
                realUsedColorIdx = 0;
            } else {
                realUsedColorIdx = currentColorIdx % n;
                if (realUsedColorIdx == 0) {
                    realUsedColorIdx = currStructure.getColorList().size() - 1;
                }
            }

            ShapeInfo shapeInfo = currStructure.getShapeInfoByColor(realUsedColorIdx);

            LinkedList<Point3d> polyPoints = shapeInfo.getPolygonPoints();
            LinkedList<TexCoord2f> polyTexCoords = shapeInfo.getPolygonTexCoords();

            polyPoints.addAll(currentVertexes);

            //texcoords
            polyTexCoords.add(BL_COORD);
            for (int i = 1; i < stripSize; i++) {
                switch (i % 4) {
                    case 0:
                    case 2:
                        polyTexCoords.add(TR_COORD);
                        break;
                    case 1:
                        polyTexCoords.add(TL_COORD);
                        break;
                    default:
                        polyTexCoords.add(BR_COORD);
                        break;
                }
            }

            shapeInfo.addPolygonStripVertexCount(stripSize);
        }

        //we're done with currentVertex
        //load outer polygon, or back to null just like the initial state
        currentVertexes = vertexes.poll();

        if (vertexes.isEmpty() && currentVertexes == null) {
            polygonMode = false;
        }
    }

    private int setColor(String currentCmds, int i) {
        //check custom index value
        i = parseValue(currentCmds, i);
        if (valued) {
            currentColorIdx = customValue.intValue();
        } else {
            currentColorIdx++;
        }
        return i;
    }

    private int move(String currentCmds, int i) {
        //check custom length value
        i = parseValue(currentCmds, i);

        //move
        if (valued) {
            bv.Move((float) (currentLength * customValue / 100));
        } else {
            bv.Move((float) currentLength);
        }
        if (prevF != null) {
            prevF.castLast();
            prevF = null;
        }
        return i;
    }

    private int decLength(String currentCmds, int i) {
        //check custom length value
        i = parseValue(currentCmds, i);

        //decrease
        if (valued) {
            currentLength = currentLength * customValue;
        } else {
            currentLength = currentLength * 0.9;
        }

        return i;
    }

    private int incLength(String currentCmds, int i) {
        //check custom length value
        i = parseValue(currentCmds, i);

        //decrease
        if (valued) {
            currentLength = currentLength * customValue;
        } else {
            currentLength = currentLength * 1.1;
        }

        return i;
    }

    private int decThickness(String currentCmds, int i) {
        //check custom length value
        i = parseValue(currentCmds, i);

        //decrease
        if (valued) {
            currentWidth = currentWidth * customValue;
        } else {
            currentWidth = currentWidth * 0.7;
        }

        return i;
    }

    private int incThickness(String currentCmds, int i) {
        //check custom length value
        i = parseValue(currentCmds, i);

        //decrease
        if (valued) {
            currentWidth = currentWidth * customValue;
        } else {
            currentWidth = currentWidth * 1.4;
        }

        return i;
    }

    private int decAngle(String currentCmds, int i) {
        //check custom length value
        i = parseValue(currentCmds, i);

        //decrease
        if (valued) {
            currentAngle = currentAngle * customValue;
        } else {
            currentAngle = currentAngle * 0.9;
        }

        return i;
    }

    private int incAngle(String currentCmds, int i) {
        //check custom length value
        i = parseValue(currentCmds, i);

        //decrease
        if (valued) {
            currentAngle = currentAngle * customValue;
        } else {
            currentAngle = currentAngle * 1.1;
        }

        return i;
    }

    private void startBranch() {
        branchingPoints.push(new BaseVector(bv));
        colorIdxes.push(currentColorIdx);
        widths.push(currentWidth);
        angles.push(currentAngle);
        lengths.push(currentLength);
        topPoints.push(prevTopPoints);
        if (prevF != null) {
            prevF.isWaitingForBranch = true;
            //because this F can't be casted by any action happened inside the branch
        }
        prevFs.push(prevF);//even if prevF is null
    }

    private void endBranch() {
        bv.set(branchingPoints.pop());
        currentColorIdx = colorIdxes.pop();
        prevTopPoints = topPoints.pop();
        currentWidth = widths.pop();
        currentAngle = angles.pop();
        currentLength = lengths.pop();
        if (prevF != null) {
            //cast last uncasted F on the closed branch, if any
            prevF.castLast();
        }
        prevF = prevFs.pop();
        if (prevF != null && prevF != prevFs.peekFirst()) {
            //second condition itu check
            //cek jangan2 ini prevF yang sama dipush ke stack berulang kali
            prevF.isWaitingForBranch = false;
        }
    }

    //turns baseVector based on last angle or custom angle
    private int turn(String currentCmds, int i, boolean isTurnRight) {

        //check custom angle value
        i = parseValue(currentCmds, i);

        double angle;
        if (valued) {
            angle = customValue;
        } else {
            angle = (float) currentAngle;
        }

        if (isTurnRight) {
            //because rotate in 3d is counterclockwise
            angle *= -1;
        }

        bv.turn(angle);

        return i;
    }

    //pitch the baseVector based on last angle or custom angle
    private int pitch(String currentCmds, int i, boolean isPitchUp) {

        //check custom angle value
        i = parseValue(currentCmds, i);

        double angle;
        if (valued) {
            angle = customValue;
        } else {
            angle = (float) currentAngle;
        }

        if (!isPitchUp) {
            angle *= -1;
        }

        bv.pitch(angle);

        return i;
    }

    //roll the baseVector on dir axis based on last angle or custom angle
    private int roll(String currentCmds, int i, boolean isClockwise) {

        //check custom angle value
        i = parseValue(currentCmds, i);

        double angle;
        if (valued) {
            angle = customValue;
        } else {
            angle = (float) currentAngle;
        }

        if (!isClockwise && !valued) {
            angle *= -1;
        }

        bv.roll(angle);

        return i;
    }

    //???
    private int rollHorizon(String currentCmds, int i) {
        double x = bv.dir.getX();//0
        double y = bv.dir.getY();//0
        double z = bv.dir.getZ();//-1

        double a = bv.v1.getX();//0
        double b = bv.v1.getY();//1
        double c = bv.v1.getZ();//0

        double az = a * z;
        double by = b * y;
        double cx = c * x;
        double param1 = 2 * az - 2 * cx;

        double s = az * az - 2 * by * by + cx * cx + b * b - 2 * a * x * by - 2 * az * cx - 2 * by * c * z;
        double sed = Math.sqrt(s);
        double denom = 2 * b * y * y + 2 * a * x * y + 2 * c * y * z - b;
        double angle = 0.0;

        boolean doNothing = false;
        if (b == 0) {
            doNothing = true;
        } else if (denom == 0) {
            if (param1 != 0) {
                double sudut3 = Math.atan(b / param1) * -2;
                angle = Math.toDegrees(sudut3);
            } else {
                doNothing = true;
            }
        } else //denom !=0
        {
            double T1 = (sed + az - cx) / denom;
            double T2 = (sed - az + cx) / denom;

            double angle1 = -2 * Math.atan(T1);
            double angle2 = 2 * Math.atan(T2);
            double s1 = Math.toDegrees(angle1);
            double s2 = Math.toDegrees(angle2);

            if (Math.abs(s1) > Math.abs(s2)) {
                angle = s2;
            } else {
                angle = s1;
            }
        }

        if (!doNothing) {
            customValue = angle;
            if (customValue >= 0) {
                i = roll(currentCmds, i, true);
            } else {
                i = roll(currentCmds, i, false);
            }
        }
        bv.normalizeAll();

        return i;
    }

    public void setAppearance(Appearance3DChangerCookie.Appearance appearance, boolean withMaterial) {
        switch (appearance) {
            case WIREFRAME: {
                WireframeAppearance wireAprProvider = new WireframeAppearance();
                structures.entrySet().stream().map((entry) -> entry.getValue()).forEach((struct) -> {
                    int colorListSize = struct.getColorList().size();
                    for (int i = 1; i < colorListSize; i++) {
                        ShapeInfo info = struct.getShapeInfoByColor(i);
                        LinkedList<Shape3D> shapes = info.getShapes();
                        shapes.stream().forEach((shape) -> {
                            shape.setAppearance(wireAprProvider.getAppearance(withMaterial));
                        });
                    }
                });
                break;
            }
            case SOLID: {
                SolidAppearance solidAprProvider = new SolidAppearance();
                structures.entrySet().stream().map((entry) -> entry.getValue()).forEach((struct) -> {
                    int colorListSize = struct.getColorList().size();
                    for (int i = 1; i < colorListSize; i++) {
                        ShapeInfo info = struct.getShapeInfoByColor(i);
                        LinkedList<Shape3D> shapes = info.getShapes();
                        shapes.stream().forEach((shape) -> {
                            shape.setAppearance(solidAprProvider.getAppearance(withMaterial));
                        });
                    }
                });
                break;
            }
            case TEXTURE: {
                TextureAppearance textureAprProvider = new TextureAppearance();
                structures.entrySet().stream().forEach((entry) -> {
                    CorakStructure struct = entry.getValue();
                    List<String> textures = struct.getTextureList();
                    int textureListSize = textures.size();
                    for (int i = 1; i < textureListSize; i++) {
                        File textureFile = new File(getTextureFilePath(textures.get(i)));
                        if (textureFile.exists()) {
                            Appearance a = textureAprProvider.getAppearance(textureFile, withMaterial);
                            ShapeInfo info = struct.getShapeInfoByColor(i);
                            info.getShapes().stream().forEach((shape) -> {
                                shape.setAppearance(a);
                            });
                        } else {
                            // TODO : handle kalau file texture yang dimaksud ga ada.
                        }
                    }
                });
                break;
            }
        }
    }

    public CorakLSystem getCor() {
        return origCor;
    }

    public CorakDataObject getCorDataObject() {
        return origCorDataObject;
    }

    public LinkedList<Point3d> getAllVertex() {
        LinkedList<Point3d> allVertexes = new LinkedList<>();
        structures.entrySet().stream().map((entry) -> entry.getValue()).forEach((struct) -> {
            int colorListSize = struct.getColorList().size();
            for (int i = 1; i < colorListSize; i++) {
                ShapeInfo info = struct.getShapeInfoByColor(i);
                LinkedList<Point3d> fPoints = info.getFPoints();
                LinkedList<Point3d> polygonPoints = info.getPolygonPoints();
                allVertexes.addAll(fPoints);
                allVertexes.addAll(polygonPoints);
            }
        });
        return allVertexes;
    }

    public HashMap<CorakLSystem, CorakStructure> getStructures() {
        return structures;
    }

    public String getTextureFolderPath() {
        FileObject thisCor = origCorDataObject.getPrimaryFile();
        FileObject textureFolder = CorakFileUtil.getTexturesFolder(FileOwnerQuery.getOwner(thisCor), false);
        return FileUtil.toFile(textureFolder).getPath();
    }

    public String getTextureFilePath(String filename) {
        return getTextureFolderPath() + File.separator + filename;
    }

    //turn on debug mode: uncomment this
    private void log(String message) {
        //System.out.println(message);
    }

    private String getCorFilePath(String value) {
        return getCorakFolderPath() + File.separator + value;
    }

    private String getCorakFolderPath() {
        FileObject thisCor = origCorDataObject.getPrimaryFile();
        FileObject coraksFolder = CorakFileUtil.getCoraksFolder(FileOwnerQuery.getOwner(thisCor), false);
        return FileUtil.toFile(coraksFolder).getPath();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //OLD METHOD AND VARIABLES THAT IS USED BY OLDER drawF
    //TO AVOID CONFLICT, AN F LETTER IS APPENDED TO EACH METHOD NAME
    private int polygonBracketsOpened = 0;
    private boolean connected = true;
    private boolean forceDisconnect = false;

    private Point3d prevFPos = new Point3d();
    private Vector3d prevFDir = new Vector3d();
    private Vector3d prevFNorm = new Vector3d();
    private Vector3d prevFV1 = new Vector3d();
    private Surface prevSurface;
    private int prevColorIdx = 1;
    private CorakStructure lastStruct;
    private boolean justChangedCor = false;

    //temporary variables
    private LinkedList<Point3d> currentFPointsByColor;
    private LinkedList<Point3d> currentPolyPointsByColor;
    private LinkedList<TexCoord2f> currentFTexCoordsByColor;
    private LinkedList<TexCoord2f> currentPolyTexCoordsByColor;
    private LinkedList<Vector3f> currentFNormalsByColor;

    //stacks for branching
    private final LinkedList<Boolean> connections = new LinkedList<Boolean>();

    public Canting(CorakLSystem cor, CorakDataObject obj, boolean old) {

        bv = new BaseVector();

        ArrayList<Color3f> colorList = new ArrayList<>();
        colorList.add(0, null);
//        colorList.add(1, new Color3f(0.0f, 0.5f, 0.0f)); //default color: dark green
//        colorList.add(2, new Color3f(0.5f, 0.0f, 0.0f));
//        colorList.add(3, new Color3f(0.0f, 0.5f, 0.5f));
//        colorList.addAll(cor.getColor3fs()); // convert dipakai disini
        colorList.addAll(cor.getColor3fs());

        ArrayList<String> textureList = new ArrayList<>();
        textureList.add(0, null);
//        textureList.add(1, "putih.jpg");
//        textureList.add(2, "biru.jpg");
//        textureList.add(3, "hijau.jpg");
        textureList.addAll(cor.getTextures());

        this.origCor = cor;
        this.currentCor = cor;
        currentLength = cor.getLength();
        currentAngle = cor.getAngle();
        currentWidth = cor.getWidth();
        this.origCorDataObject = obj;

        structures = new HashMap<>();
        structures.put(origCor, new CorakStructure(colorList, textureList));
        currStructure = structures.get(cor);
    }

    /**
     * Main access to generate() method, using Axiom and initial iteration as
     * params. After the recursion ends, all shapes will be generated. Then
     * attach the generated shapes to mainTG.
     */
    public void generateF(boolean isForOBJ) {

        this.encloseOBJ = isForOBJ;

        batikBG = new BranchGroup();
        batikBG.setCapability(BranchGroup.ALLOW_DETACH);
        batikBG.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
        batikBG.setCapability(BranchGroup.ALLOW_BOUNDS_WRITE);
        batikBG.setCapability(BranchGroup.ALLOW_AUTO_COMPUTE_BOUNDS_READ);
        batikBG.setCapability(BranchGroup.ALLOW_AUTO_COMPUTE_BOUNDS_WRITE);
        batikTG = new TransformGroup();
        batikTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        batikTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        batikTG.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        batikTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        batikTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        batikTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        batikTG.setCapability(TransformGroup.ALLOW_BOUNDS_READ);
        batikBG.addChild(batikTG);
        batikBG.setBoundsAutoCompute(true);

        missingExternal.clear();
        prevF = null;
        generate(currentCor.getAxiom(), currentCor.getIteration());
        if (prevF != null) {
            prevF.castLast();
        }
        //last top cap
        if (isForOBJ) {
            prevSurface = currentSurface;
            drawFTopCap();
        }

        structures.entrySet().stream()
                .map((entry) -> entry.getValue())
                .map((struct) -> {
                    ArrayList<Color3f> colorList = struct.getColorList();
                    int colorListSize = colorList.size();
                    for (int idx = 1; idx < colorListSize; idx++) {
                        ShapeInfo shapeInfo = struct.getShapeInfoByColor(idx);
                        Color3f color = colorList.get(idx);
                        //create F
                        if (shapeInfo.getFPoints().size() > 0) {
                            Point3d[] fPoints = shapeInfo.getFPointsInArray();
                            Color3f[] colors = new Color3f[fPoints.length];
                            Arrays.fill(colors, new Color3f(color));

                            QuadArray quadArray = new QuadArray(
                                    fPoints.length,
                                    QuadArray.COORDINATES
                                    | QuadArray.COLOR_3
                                    | QuadArray.NORMALS
                                    | QuadArray.TEXTURE_COORDINATE_2
                            );
                            quadArray.setCoordinates(0, fPoints);
                            quadArray.setNormals(0, shapeInfo.getFNormalsInArray());
                            quadArray.setTextureCoordinates(0, 0, shapeInfo.getFTexCoordsInArray());
                            quadArray.setColors(0, colors);

                            Shape3D F = new Shape3D();
                            F.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
                            F.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
                            F.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_READ);
                            F.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE);
                            F.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
                            F.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

                            //form the shape using the quad array
                            F.setGeometry(quadArray);

                            WireframeAppearance wireAprProvider = new WireframeAppearance();
                            F.setAppearance(wireAprProvider.getAppearance());

                            batikTG.addChild(F);
                            shapeInfo.setFShape(F);
                        }

                        //create polygon
                        if (shapeInfo.getPolygonPoints().size() > 0) {
                            Point3d[] polygonPoints = shapeInfo.getPolygonPointsInArray();

                            TriangleFanArray tfArray = new TriangleFanArray(
                                    polygonPoints.length,
                                    TriangleFanArray.COORDINATES
                                    | TriangleFanArray.COLOR_3
                                    | TriangleFanArray.TEXTURE_COORDINATE_2,
                                    shapeInfo.getPolygonStripVertexCountsInArray()
                            );

                            Color3f[] colorsForPolygon = new Color3f[polygonPoints.length];
                            Arrays.fill(colorsForPolygon, color);

                            tfArray.setCoordinates(0, polygonPoints);
                            tfArray.setTextureCoordinates(0, 0, shapeInfo.getPolygonTexCoordsInArray());
                            tfArray.setColors(0, colorsForPolygon);

                            //generate normals automatically
                            GeometryInfo gi = new GeometryInfo(tfArray);
                            NormalGenerator normalGenerator = new NormalGenerator(0);
                            normalGenerator.generateNormals(gi);

                            Shape3D polygon = new Shape3D();
                            polygon.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
                            polygon.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
                            polygon.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_READ);
                            polygon.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE);
                            polygon.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
                            polygon.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

                            //set geometry with generated normals
                            polygon.setGeometry(gi.getGeometryArray());

                            WireframeAppearance wireAprProvider = new WireframeAppearance();
                            polygon.setAppearance(wireAprProvider.getAppearance());

                            batikTG.addChild(polygon);
                            shapeInfo.setPolygonShape(polygon);
                        }

                        //create enclose, if necessary
                        if (shapeInfo.getEnclosePoints().size() > 0) {
                            Point3d[] enclosePoints = shapeInfo.getEnclosePointsInArray();

                            GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);

                            //set coordinates
                            gi.setCoordinates(enclosePoints);
                            gi.setStripCounts(shapeInfo.getEncloseStripVertexCountsInArray());

                            Color3f[] colorsForEnclose = new Color3f[enclosePoints.length];
                            Arrays.fill(colorsForEnclose, color);

                            //set colors
                            gi.setColors(colorsForEnclose);

                            //TODO : set Texture Coordinate?
                            //generate normal automagically
                            NormalGenerator ng = new NormalGenerator();
                            ng.generateNormals(gi);

                            //generate strips automagically
                            Stripifier st = new Stripifier();
                            st.stripify(gi);

                            Shape3D enclose = new Shape3D();
                            WireframeAppearance wireAprProvider = new WireframeAppearance();
                            enclose.setAppearance(wireAprProvider.getAppearance());
                            enclose.setGeometry(gi.getGeometryArray());

                            batikTG.addChild(enclose);
                            shapeInfo.setEnclosingShape(enclose);
                        }

                        struct.setShapeInfoByColor(idx, shapeInfo);

                    }
                    return struct;
                }).forEach((_item) -> {
                    for (int i = 0; i < polygonBracketsOpened; i++) {
                        endPolygon();
                    }
                });
    }

    /**
     * To generate shapes of external .cor file as in *(daun.cor). This method
     * put the original cor down, draw another cor, take the original cor again.
     *
     * @param externalCorPath
     */
    private void generateCorF(String externalCorPath) {
        if (missingExternal.contains(externalCorPath)) {
            return;
        }
        //simpan kondisi mula-mula
        CorakLSystem lastCor = currentCor;
        double lastAngle = currentAngle;
        double lastWidth = currentWidth;
        double lastLength = currentLength;
        int lastColorIdx = currentColorIdx;
        Surface lastSurface = currentSurface;
        BaseVector lastBV = new BaseVector(bv);
        lastStruct = currStructure;

        justChangedCor = true;

        //load kondisi corak external
        //pertama-tama cek dari loadedCor
        CorakLSystem candidate = loadedCor.get(externalCorPath);
        if (candidate != null) {
            currentCor = candidate;
        } else {
            //belum pernah di load sebelumnya
            CorakLSystem newCor = CorakSerializer.deserialize(externalCorPath);
            currentCor = newCor;
            loadedCor.put(externalCorPath, newCor);
        }
        currentAngle = currentCor.getAngle();
        currentWidth = currentCor.getWidth();
        currentLength = currentCor.getLength();
        currentColorIdx = 1;
        connected = false;
        forceDisconnect = true;
        currentSurface = new Tube();

        ArrayList<Color3f> extColorList = new ArrayList<>();
        extColorList.add(0, null);
        extColorList.addAll(currentCor.getColor3fs());

        ArrayList<String> extTexList = new ArrayList<>();
        extTexList.add(0, null);
        extTexList.addAll(currentCor.getTextures());

        if (candidate == null) {
            //belum pernah di load sebelumnya
            structures.put(currentCor, new CorakStructure(extColorList, extTexList));
        }
        currStructure = structures.get(currentCor);

        F lastF = prevF;

        prevF = null;
        generate(currentCor.getAxiom(), currentCor.getIteration());
        if (prevF != null) {
            //cast last uncasted F on loaded cor
            prevF.castLast();
        }
        prevF = lastF;
        currentCor = lastCor;
        currentAngle = lastAngle;
        currentWidth = lastWidth;
        currentLength = lastLength;
        currentColorIdx = lastColorIdx;
        bv.set(lastBV);
        connected = false;
        forceDisconnect = true;
        currentSurface = lastSurface;

        currStructure = lastStruct;

        for (int i = 0; i < polygonBracketsOpened; i++) {
            endPolygon();
        }
    }

    /**
     * fills customValue or externalCor
     *
     * @param currentCmds
     * @param i
     * @return new i after parsing the value
     */
    private int parseValueF(String currentCmds, int i) {

        skipExtCor = false;

        String value = "";

        valued = false;
        if (i + 1 < currentCmds.length()) {
            if (currentCmds.charAt(i + 1) == '(') {
                i += 2;
                while (currentCmds.charAt(i) != ')') {
                    value = value + currentCmds.charAt(i);
                    i++;
                }
                try {
                    customValue = Double.parseDouble(value);
                    log("value= " + customValue);
                } catch (NumberFormatException e) {
                    //not a float, must be name of external .cor file
                    String externalCorPath = getCorFilePath(value);
                    File externalCorFile = new File(externalCorPath);
                    if (!missingExternal.contains(externalCorFile.getAbsolutePath())) {
                        if (!externalCorFile.exists()) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                                    "File can't be found: " + externalCorFile.getAbsolutePath(),
                                    "Notice",
                                    NotifyDescriptor.DEFAULT_OPTION,
                                    NotifyDescriptor.WARNING_MESSAGE));
                            missingExternal.add(externalCorFile.getAbsolutePath());
                        } else {
                            if (value.toLowerCase().endsWith(".cor")) {

                                //if there's cyclic reference
                                if (externalCorFile.getName().equals(origCorDataObject.getPrimaryFile().getNameExt())) {
                                    if (!lastIgnored.equals(externalCorFile.getName())) {
                                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                                                "The symbol \n*(" + origCorDataObject.getPrimaryFile().getNameExt() + ")\nis ignored because of cyclic reference.\nTry not to load a Corak within itself.",
                                                "Notice",
                                                NotifyDescriptor.DEFAULT_OPTION,
                                                NotifyDescriptor.WARNING_MESSAGE));
                                    }
                                    skipExtCor = true;
                                    lastIgnored = externalCorFile.getName();
                                }

                                if (!skipAll) {
                                    externalCor = externalCorFile.getAbsolutePath();
                                }

                                //test apakah ini valid path apa engga
                                if (CorakSerializer.deserialize(externalCorFile.getAbsolutePath()) == null) {
                                    skipExtCor = true;
                                    skipAll = true;
                                    missingExternal.add(externalCorFile.getAbsolutePath());
                                }

                                log("path= " + externalCorPath);
                            } else if (value.toLowerCase().endsWith(".obj")) {
                                externalCor = externalCorFile.getAbsolutePath();
                            }
                        }
                    }
                }

                if (customValue != null) {
                    valued = true;
                }
            }
        }

        //now index of "for" has jumped forward to ')'
        return i;
    }

    private int parseFUnitF(String currentCmds, int i) {

        String value = "";

        valued = false;
        if (i + 1 < currentCmds.length()) {
            if (currentCmds.charAt(i + 1) == '(') {
                i += 2;
                while (currentCmds.charAt(i) != ')') {
                    value = value + currentCmds.charAt(i);
                    i++;
                }
                if (value.length() > 0) {
                    valued = true;
                    if ((currentSurfaceName == null) || (currentSurfaceName != null && !currentSurfaceName.equals(value))) {
                        currentSurfaceName = value;
                        connected = false;
                    }
                }
            }
        }

        //now index of "for" has jumped forward to ')'
        return i;
    }

    private int drawF(String currentCmds, int i) {

        //get the actual color index to be used
        int tempColorIdx;
        int n = (currStructure.getColorList().size() - 1);
        if (n == 0) {
            tempColorIdx = 0;
        } else {
            tempColorIdx = currentColorIdx % n;
            if (tempColorIdx == 0) {
                tempColorIdx = currStructure.getColorList().size() - 1;
            }
        }

        //new set of points (new tube)
        ShapeInfo shapeInfo = currStructure.getShapeInfoByColor(tempColorIdx);
        currentFPointsByColor = shapeInfo.getFPoints();
        currentFNormalsByColor = shapeInfo.getFNormals();
        currentFTexCoordsByColor = shapeInfo.getFTexCoords();

        i = parseFUnit(currentCmds, i); // currentSurfaceName <-- name inside the brackets
        boolean surfaceFound = false;

        if (valued) {
            //F(something)
            List<Surface> surfaceList = currentCor.getSurfaces();
            for (Surface surface : surfaceList) {
                if (surface.getName().equals(currentSurfaceName)) {
                    surfaceFound = true;
                    currentSurface = surface;
                    break;
                }
            }

            if (!surfaceFound
                    && !tempSurfaceNotExists.contains(currentSurfaceName)) { //prevents error popup shown multiple times
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                        "FUnit (" + currentSurfaceName + ") is not exists.\nUsing previous FUnit instead.",
                        "Notice",
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.WARNING_MESSAGE));
                tempSurfaceNotExists.add(currentSurfaceName);
            }
        }

        //now we know the number of points in currentSurface,
        //check again with previous top points, connect them again if they have same amount
        if (prevTopPoints != null && !forceDisconnect) {
            if (currentSurface != null) {
                if (prevTopPoints.length == currentSurface.getPoints().length) {
                    connected = true;
                }
            } else { //no currentSurface -> default F with 8 points
                if (prevTopPoints.length == 8) {
                    connected = true;
                }
            }
        }
        forceDisconnect = false;

        //if connected with previous F, use the previous F's top points as 
        //bottom points
        float r = (float) currentWidth;
        Point3d[] botPts;
        if (connected && prevTopPoints != null) {
            botPts = prevTopPoints;
        } else {
            if (encloseOBJ) {
                if (prevTopPoints != null) {
                    drawFTopCap();
                }
                drawFBottomCap();
            }
            botPts = bv.vRound(r, currentSurface);
        }

        Point3d botCenter = new Point3d(bv.center);

        bv.Move(currentLength);

        Point3d topCenter = new Point3d(bv.center);

        //top surface
        Point3d[] topPts = bv.vRound(r, currentSurface);

        prevTopPoints = topPts;

        int pointsnum = currentSurface.pointsNum();

        //filling the coordinates
        for (int j = 0, k = 0; j < pointsnum * 4; j += 4, k++) {
            Point3d nextBotPts = botPts[(k + 1) % pointsnum];
            Point3d nextTopPts = topPts[(k + 1) % pointsnum];

            assignToQuadArray(botPts[k], botCenter, new TexCoord2f(0.0f, 0.0f));

            assignToQuadArray(nextBotPts, botCenter, new TexCoord2f(1.0f, 0.0f));

            assignToQuadArray(nextTopPts, topCenter, new TexCoord2f(1.0f, 1.0f));

            assignToQuadArray(topPts[k], topCenter, new TexCoord2f(0.0f, 1.0f));

        }

        connected = true;

        prevFPos.set(bv.center);
        prevFDir.set(bv.dir);
        prevFNorm.set(bv.norm);
        prevFV1.set(bv.v1);
        prevColorIdx = currentColorIdx;
        prevSurface = currentSurface;
        lastStruct = currStructure;

        return i;
    }

    private void assignToQuadArray(Point3d point, Point3d bvCenter, TexCoord2f texCorner) {
        currentFPointsByColor.add(point);

        Vector3d tempNormal = new Vector3d();
        tempNormal.sub(point, bvCenter);
        currentFNormalsByColor.add(new Vector3f(tempNormal));
        currentFTexCoordsByColor.add(texCorner);
    }

    private void startPolygonF() {
        polygonMode = true;
        polygonBracketsOpened++;
        if (currentVertexes != null) {
            vertexes.push(new LinkedList<>(currentVertexes));
        }
        currentVertexes = new LinkedList<>();
        recordVertex();

    }

    private void endPolygonF() {

        if (currentVertexes != null && currentVertexes.size() >= 3) {

            int tempColorIdx;
            int n = (currStructure.getColorList().size() - 1);
            if (n == 0) {
                tempColorIdx = 0;
            } else {
                tempColorIdx = currentColorIdx % n;
                if (tempColorIdx == 0) {
                    tempColorIdx = currStructure.getColorList().size() - 1;
                }
            }

            int stripSize = currentVertexes.size();

            ShapeInfo shapeInfoByColor = currStructure.getShapeInfoByColor(tempColorIdx);

            currentPolyPointsByColor = shapeInfoByColor.getPolygonPoints();
            currentPolyTexCoordsByColor = shapeInfoByColor.getPolygonTexCoords();

            currentPolyPointsByColor.addAll(currentVertexes);

            //texcoords
            currentPolyTexCoordsByColor.add(new TexCoord2f(0.0f, 0.0f));
            for (int i = 1; i < stripSize; i++) {
                switch (i % 4) {
                    case 0:
                    case 2:
                        currentPolyTexCoordsByColor.add(new TexCoord2f(1.0f, 1.0f));
                        break;
                    case 1:
                        currentPolyTexCoordsByColor.add(new TexCoord2f(0.0f, 1.0f));
                        break;
                    default:
                        currentPolyTexCoordsByColor.add(new TexCoord2f(1.0f, 0.0f));
                        break;
                }
            }

            shapeInfoByColor.addPolygonStripVertexCount(stripSize);

            if (!vertexes.isEmpty()) {
                currentVertexes = vertexes.pop();
            }
        } else {
            if (currentVertexes != null && currentVertexes.size() > 0) {
                currentVertexes.clear();
            }
        }

        if (polygonBracketsOpened > 0) {
            polygonBracketsOpened--;
        }
        if (polygonBracketsOpened == 0) {
            polygonMode = false;
        }

    }

    private void drawFTopCap() {
        //close prev F with flat F
        //bottom points -> prev top points
        //top points -> flatten version of prev surface
        //cap -> enclose top point with polygon

        //use the actual color index to be used, because at this point the color was probably changed
        CorakStructure tempStrc = currStructure;
        if (justChangedCor) {
            justChangedCor = false;
            currStructure = lastStruct;
        }

        int tempColorIdx;
        int n = (currStructure.getColorList().size() - 1);
        if (n == 0) {
            tempColorIdx = 0;
        } else {
            tempColorIdx = prevColorIdx % n;
            if (tempColorIdx == 0) {
                tempColorIdx = currStructure.getColorList().size() - 1;
            }
        }

        ShapeInfo shapeInfo = currStructure.getShapeInfoByColor(tempColorIdx);
        currentFPointsByColor = shapeInfo.getFPoints();
        currentFNormalsByColor = shapeInfo.getFNormals();
        currentFTexCoordsByColor = shapeInfo.getFTexCoords();

        Point3d pos = new Point3d(bv.center);
        Vector3d dir = new Vector3d(bv.dir);
        Vector3d norm = new Vector3d(bv.norm);
        Vector3d v1 = new Vector3d(bv.v1);
        bv.Move(prevFPos);
        bv.dir.set(prevFDir);
        bv.norm.set(prevFNorm);
        bv.v1.set(prevFV1);
        float r = (float) currentWidth;

        Point3d[] botPts = bv.vRound(r, prevSurface);
        Point3d botCenter = new Point3d(bv.center);
        Vector3d bvnormal = new Vector3d(bv.dir);

        //calculate distance of the farthest botPts point in front of bv
        double D = -(bvnormal.x * botCenter.x + bvnormal.y * botCenter.y + bvnormal.z * botCenter.z);
        double distance = 0;
        for (int i = 0; i < botPts.length; i++) {
            //if in font of bv.center plane, check distance, find the farthest
            if ((bvnormal.x * botPts[i].x + bvnormal.y * botPts[i].y + bvnormal.z * botPts[i].z + D) > 0) {
                double tempDistance = Math.abs(bvnormal.dot(new Vector3d(botPts[i].x - botCenter.x, botPts[i].y - botCenter.y, botPts[i].z - botCenter.z)));
                if (tempDistance > distance) {
                    distance = tempDistance;
                }
            }
        }

        bv.Move(distance);

        Surface flatSurface = createFlatSurface(prevSurface);
        Point3d[] topPts = bv.vRound(r, flatSurface);
        Point3d topCenter = new Point3d(bv.center);

        int pointsnum = prevSurface.pointsNum();

        //filling the coordinates
        for (int j = 0, k = 0; j < pointsnum * 4; j += 4, k++) {
            Point3d nextBotPts = botPts[(k + 1) % pointsnum];
            Point3d nextTopPts = topPts[(k + 1) % pointsnum];

            assignToQuadArray(botPts[k], botCenter, new TexCoord2f(0.0f, 0.0f));

            assignToQuadArray(nextBotPts, botCenter, new TexCoord2f(1.0f, 0.0f));

            assignToQuadArray(nextTopPts, topCenter, new TexCoord2f(1.0f, 1.0f));

            assignToQuadArray(topPts[k], topCenter, new TexCoord2f(0.0f, 1.0f));

        }

        tempColorIdx = currentColorIdx;
        currentColorIdx = prevColorIdx;

        bv.Move(topCenter);
        startPolygon();
        for (int i = 0; i < topPts.length; i++) {
            bv.Move(topPts[i]);
            recordVertex();
        }
        bv.Move(topPts[0]);
        recordVertex();
        endPolygon();
        bv.Move(pos);
        bv.dir.set(dir);
        bv.norm.set(norm);
        bv.v1.set(v1);

        //restore color index
        currStructure = tempStrc;
        currentColorIdx = tempColorIdx;

        n = (currStructure.getColorList().size() - 1);
        if (n == 0) {
            tempColorIdx = 0;
        } else {
            tempColorIdx = currentColorIdx % n;
            if (tempColorIdx == 0) {
                tempColorIdx = currStructure.getColorList().size() - 1;
            }
        }

        //restore color index
        shapeInfo = currStructure.getShapeInfoByColor(tempColorIdx);
        currentFPointsByColor = shapeInfo.getFPoints();
        currentFNormalsByColor = shapeInfo.getFNormals();
        currentFTexCoordsByColor = shapeInfo.getFTexCoords();
    }

    private void drawFBottomCap() {
        //start next F with flat F
        //bottom points -> flatten version of current surface
        //top points -> vRound, new points  <-- next F, please take this
        //cap -> enclose bottom point with polygon

        float r = (float) currentWidth;

        Point3d[] botPts = bv.vRound(r, currentSurface);
        Point3d botCenter = new Point3d(bv.center);
        Vector3d bvnormal = new Vector3d(bv.dir);

        //calculate distance of the farthest botPts point in back of bv
        double D = -(bvnormal.x * botCenter.x + bvnormal.y * botCenter.y + bvnormal.z * botCenter.z);
        double distance = 0;
        for (int i = 0; i < botPts.length; i++) {
            //if in back of bv.center plane, check distance, find the farthest
            if ((bvnormal.x * botPts[i].x + bvnormal.y * botPts[i].y + bvnormal.z * botPts[i].z + D) < 0) {
                double tempDistance = Math.abs(bvnormal.dot(new Vector3d(botPts[i].x - botCenter.x, botPts[i].y - botCenter.y, botPts[i].z - botCenter.z)));
                if (tempDistance > distance) {
                    distance = tempDistance;
                }
            }
        }

        bv.Move(-distance);
        Surface flatSurface = createFlatSurface(currentSurface);
        botPts = bv.vRound(r, flatSurface);
        botCenter.set(bv.center);
        bv.Move(distance);

        Point3d[] topPts = bv.vRound(r, currentSurface);
        Point3d topCenter = new Point3d(bv.center);

        int pointsnum = currentSurface.pointsNum();

        //filling the coordinates
        for (int j = 0, k = 0; j < pointsnum * 4; j += 4, k++) {
            Point3d nextBotPts = botPts[(k + 1) % pointsnum];
            Point3d nextTopPts = topPts[(k + 1) % pointsnum];

            assignToQuadArray(botPts[k], botCenter, new TexCoord2f(0.0f, 0.0f));

            assignToQuadArray(nextBotPts, botCenter, new TexCoord2f(1.0f, 0.0f));

            assignToQuadArray(nextTopPts, topCenter, new TexCoord2f(1.0f, 1.0f));

            assignToQuadArray(topPts[k], topCenter, new TexCoord2f(0.0f, 1.0f));

        }

        bv.Move(botCenter);
        startPolygon();
        for (int i = 0; i < botPts.length; i++) {
            bv.Move(botPts[i]);
            recordVertex();
        }
        bv.Move(botPts[0]);
        recordVertex();
        endPolygon();
        bv.Move(topCenter);
    }

    private Surface createFlatSurface(Surface sfc) {
        Surface flatSurface = new Surface();
        Point3d[] currSurfacePoints = sfc.getPoints();
        Point3d[] points = new Point3d[currSurfacePoints.length];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point3d(currSurfacePoints[i].x, currSurfacePoints[i].y, 0);
        }
        flatSurface.setPoints(points);
        return flatSurface;
    }

    private int moveF(String currentCmds, int i) {
        //check custom length value
        i = parseValue(currentCmds, i);

        //move
        if (valued) {
            bv.Move((float) (currentLength * customValue / 100));
        } else {
            bv.Move((float) currentLength);
        }
        if (prevF != null) {
            prevF.castLast();
            prevF = null;
        }
        connected = false;
        forceDisconnect = true;
        return i;
    }

    private void startBranchF() {
        branchingPoints.push(new BaseVector(bv));
        colorIdxes.push(currentColorIdx);
        widths.push(currentWidth);
        angles.push(currentAngle);
        lengths.push(currentLength);
        topPoints.push(prevTopPoints);
        connections.push(connected);
        if (prevF != null) {
            prevF.isWaitingForBranch = true;
            //because this F can't be casted by any action happened inside the branch
        }
        prevFs.push(prevF);//even if prevF is null
    }

    private void endBranchF() {
        bv.set(branchingPoints.pop());
        currentColorIdx = colorIdxes.pop();
        prevTopPoints = topPoints.pop();
        currentWidth = widths.pop();
        currentAngle = angles.pop();
        currentLength = lengths.pop();
        connected = connections.pop();
        if (prevF != null) {
            //cast last uncasted F on the closed branch, if any
            prevF.castLast();
        }
        prevF = prevFs.pop();
        if (prevF != null) {
            prevF.isWaitingForBranch = false;
        }
    }
}
