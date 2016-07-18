import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nikitasonthalia on 6/15/16.
 */

/**
 * This is the K-D tree node.
 */
class KDNode
{
    int axis;
    double[] x;
    String data;
    int id;
    boolean checked;
    boolean orientation;

    KDNode Parent;
    KDNode Left;
    KDNode Right;

    public KDNode(double[] x0, int axis0,String data0)
    {
        x = new double[2];
        axis = axis0;
        for (int k = 0; k < 2; k++)
            x[k] = x0[k];
        data = data0;
        Left = Right = Parent = null;
        checked = false;
        id = 0;
    }

    public KDNode FindParent(double[] x0)
    {
        KDNode parent = null;
        KDNode next = this;
        int split;
        while (next != null)
        {
            split = next.axis;
            parent = next;
            if (x0[split] > next.x[split])
                next = next.Right;
            else
                next = next.Left;
        }
        return parent;
    }

    public KDNode Insert(double[] p, String data0)
    {
        x = new double[2];
        KDNode parent = FindParent(p);
        if (equal(p, parent.x, 2) == true)
            return null;

        KDNode newNode = new KDNode(p,
                parent.axis + 1 < 2 ? parent.axis + 1 : 0,data0);
        newNode.Parent = parent;

        if (p[parent.axis] > parent.x[parent.axis])
        {
            parent.Right = newNode;
            newNode.orientation = true; //
        } else
        {
            parent.Left = newNode;
            newNode.orientation = false; //
        }

        return newNode;
    }

    boolean equal(double[] x1, double[] x2, int dim)
    {
        for (int k = 0; k < dim; k++)
        {
            if (x1[k] != x2[k])
                return false;
        }

        return true;
    }

    double distance2(double[] x1, double[] x2, int dim)
    {
        double S = 0;
        for (int k = 0; k < dim; k++)
            S += (x1[k] - x2[k]) * (x1[k] - x2[k]);
        return S;
    }
}

/**
 * This K-D Tree.
 */
class KDTree
{
    KDNode Root;

//    int TimeStart, TimeFinish;
//    int CounterFreq;

    double d_min;
    KDNode nearest_neighbour;

    int KD_id;

    int nList;

    KDNode CheckedNodes[];
    int checked_nodes;
    //KDNode List[];

    double x_min[], x_max[];
    boolean max_boundary[], min_boundary[];
    int n_boundary;

    public KDTree(int i)
    {
        Root = null;
        KD_id = 1;
        nList = 0;
        //List = new KDNode[i];
        CheckedNodes = new KDNode[i];
        max_boundary = new boolean[2];
        min_boundary = new boolean[2];
        x_min = new double[2];
        x_max = new double[2];
    }

    public boolean add(double[] x, String data0)
    {
        if (nList >= 2000000 - 1)
            return false; // can't add more points

        if (Root == null)
        {
            Root = new KDNode(x, 0,data0);
            Root.id = KD_id++;
           // List[nList++] = Root;
        } else
        {
            KDNode pNode;
            if ((pNode = Root.Insert(x,data0)) != null)
            {
                pNode.id = KD_id++;
                //List[nList++] = pNode;
            }
        }

        return true;
    }

    public KDNode find_nearest(double[] x)
    {
        if (Root == null)
            return null;

        checked_nodes = 0;
        KDNode parent = Root.FindParent(x);
        nearest_neighbour = parent;
        d_min = Root.distance2(x, parent.x, 2);
        ;

        if (parent.equal(x, parent.x, 2) == true)
            return nearest_neighbour;

        search_parent(parent, x);
        uncheck();

        return nearest_neighbour;
    }

    public void check_subtree(KDNode node, double[] x)
    {
        if ((node == null) || node.checked)
            return;

        CheckedNodes[checked_nodes++] = node;
        node.checked = true;
        set_bounding_cube(node, x);

        int dim = node.axis;
        double d = node.x[dim] - x[dim];

        if (d * d > d_min)
        {
            if (node.x[dim] > x[dim])
                check_subtree(node.Left, x);
            else
                check_subtree(node.Right, x);
        } else
        {
            check_subtree(node.Left, x);
            check_subtree(node.Right, x);
        }
    }

    public void set_bounding_cube(KDNode node, double[] x)
    {
        if (node == null)
            return;
        int d = 0;
        double dx;
        for (int k = 0; k < 2; k++)
        {
            dx = node.x[k] - x[k];
            if (dx > 0)
            {
                dx *= dx;
                if (!max_boundary[k])
                {
                    if (dx > x_max[k])
                        x_max[k] = dx;
                    if (x_max[k] > d_min)
                    {
                        max_boundary[k] = true;
                        n_boundary++;
                    }
                }
            } else
            {
                dx *= dx;
                if (!min_boundary[k])
                {
                    if (dx > x_min[k])
                        x_min[k] = dx;
                    if (x_min[k] > d_min)
                    {
                        min_boundary[k] = true;
                        n_boundary++;
                    }
                }
            }
            d += dx;
            if (d > d_min)
                return;

        }

        if (d < d_min)
        {
            d_min = d;
            nearest_neighbour = node;
        }
    }

    public KDNode search_parent(KDNode parent, double[] x)
    {
        for (int k = 0; k < 2; k++)
        {
            x_min[k] = x_max[k] = 0;
            max_boundary[k] = min_boundary[k] = false; //
        }
        n_boundary = 0;

        KDNode search_root = parent;
        while (parent != null && (n_boundary != 2 * 2))
        {
            check_subtree(parent, x);
            search_root = parent;
            parent = parent.Parent;
        }

        return search_root;
    }

    public void uncheck()
    {
        for (int n = 0; n < checked_nodes; n++)
            CheckedNodes[n].checked = false;
    }

    public void inorder()
    {
        inorder(Root);
    }

    private void inorder(KDNode root)
    {
        if (root != null)
        {
            inorder(root.Left);
            System.out.print("(" + root.x[0] + ", " + root.x[1] + ","+ root.data +","+ root.id + ")  ");
            inorder(root.Right);
        }
    }

//    public void preorder()
//    {
//        preorder(Root);
//    }
//
//    private void preorder(KDNode root)
//    {
//        if (root != null)
//        {
//            System.out.print("(" + root.x[0] + ", " + root.x[1] + ","+ root.id + ")  ");
//            inorder(root.Left);
//            inorder(root.Right);
//        }
//    }
//
//    public void postorder()
//    {
//        postorder(Root);
//    }
//
//    private void postorder(KDNode root)
//    {
//        if (root != null)
//        {
//            inorder(root.Left);
//            inorder(root.Right);
//            System.out.print("(" + root.x[0] + ", " + root.x[1] + ")  ");
//        }
//    }

    public String find_nearest_neibour_data(double[] x) {
        KDNode nearestNeibour=find_nearest(x);
        return nearestNeibour.data;
    }
}

/**
 * This is main function.
 */

public class P2 {

    static boolean flagX=false;
    static boolean flagY=false;
    static double mapValue1=0.0;
    static double mapValue2=0.0;
    static Map<String,Double> mapping1=new HashMap<String, Double>();
    static Map<String,Double> mapping2=new HashMap<String, Double>();
    /**
     * This method is for validating csv data file and validate that the data contain required value.
     *
     * @param token is the , separated string array.
     * @return true if data are valid else false.
     */
    public static int validateData(String token[]) {


        if (token.length == 3) {
            if (token[0].matches("\\d+\\.\\d+") && flagX==true)
            {
                return 0;
            }
            if (token[1].matches("\\d+\\.\\d+") && flagY==true)
            {
                return 0;
            }
            if(token[0].matches("\\w+"))
            {
                flagX=true;
                if(!mapping1.containsKey(token[0]))
                {
                    mapping1.put(token[0],++mapValue1);
                }
                token[0]=mapping1.get(token[0]).toString();
            }
            if(token[1].matches("\\w+"))
            {
                flagY=true;
                if(!mapping2.containsKey(token[1]))
                {
                    mapping2.put(token[1],++mapValue2);
                }
                token[1]=mapping2.get(token[1]).toString();
            }
            if (token[0].matches("\\d+\\.\\d+") && token[1].matches("\\d+\\.\\d+") && token[2].matches("\\w+") ) {

                return 3;
            } else {


                return 0;
            }
        } else {

            if (token.length == 2)
            {
                if (token[0].matches("\\d+\\.\\d+") && flagX==true)
                {
                    return 0;
                }
                if (token[1].matches("\\d+\\.\\d+") && flagY==true)
                {
                    return 0;
                }
                if(token[0].matches("\\w+"))
                {
                    flagX=true;
                    if(!mapping1.containsKey(token[0]))
                    {
                        mapping1.put(token[0],++mapValue1);
                    }
                    token[0]=mapping1.get(token[0]).toString();
                }
                if(token[1].matches("\\w+"))
                {
                    flagY=true;
                    if(!mapping2.containsKey(token[1]))
                    {
                        mapping2.put(token[1],++mapValue2);
                    }
                    token[1]=mapping2.get(token[1]).toString();
                }
                if (token[0].matches("\\d+\\.\\d+") && token[1].matches("\\d+\\.\\d+")) {

                    return 2;
                }
            }
            else
            {
                return 0;
            }
        }
        // {

        //}
        // else
        // {
        //     return false;
        // }
        return 0;
    }

    /**
     * This method remove comments and space, tab etc.
     *
     * @param line is the data read from file.
     * @return string removed all comments space etc.
     */
    public static String removeCommentAndSpace(String line) {
        Pattern pattern = Pattern.compile("#.*$", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(line);

        line = matcher.replaceFirst("");
        line = line.replaceAll("\\t+", " ");
        line = line.replaceAll("\\s+", " ");
        line = line.replaceAll("\\n", "");
        return line;
    }

    public static String getKey(double value, Map<String,Double> map)
    {
        for(Map.Entry<String,Double> ee : map.entrySet())
        {

            if(ee.getValue() ==value)
            {
                return ee.getKey();
            }
        }

        return null;
    }

    public static void main(String[] args) {
        BufferedReader in = null;
        String line;

        int count = 0;
        int countP = 0;
        ArrayList<Double> valPX = new ArrayList<Double>();
        ArrayList<Double> valPY = new ArrayList<Double>();
        ArrayList<Double> valX = new ArrayList<Double>();
        ArrayList<Double> valY = new ArrayList<Double>();
        ArrayList<String> output = new ArrayList<String>();




        try {
            //in = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new FileReader("data.txt"));
            while ((line = in.readLine()) != null) {
                line = removeCommentAndSpace(line);
                if (!line.isEmpty()) {
                    String[] token = line.split(" ");
                    int validity=validateData(token);
                    if (validity== 3) {
                        valX.add(count, Double.parseDouble(token[0].toString()));
                        valY.add(count, Double.parseDouble(token[1].toString()));
                        output.add(count, token[2]);
                        count++;

                    } else {
                        if (validity == 2) {

                            valPX.add(countP, Double.parseDouble(token[0].toString()));
                            valPY.add(countP, Double.parseDouble(token[1].toString()));
                            countP++;

                        } else {

                            System.out.println("Data is not in requried format.");
                            System.exit(1);

                        }
                    }
                }
            }


            int numpoints = count;

            KDTree kdt = new KDTree(numpoints);
            double x[] = new double[2];
            for (int i = 0; i < count; i++) {
                x[0] = (double) valX.get(i);
                x[1] = (double) valY.get(i);

                kdt.add(x, (String) output.get(i));
            }
            for (int i =0; i<countP;i++) {
                x[0] = valPX.get(i);
                x[1] = valPY.get(i);
                String data = kdt.find_nearest_neibour_data(x);
                if(!mapping1.isEmpty())
                {
                    if (!mapping2.isEmpty())
                    {
                        System.out.println( getKey(x[0],mapping1) + " " + getKey(x[1],mapping2) + " " + data);
                    }
                    else
                    {
                        System.out.println( getKey(x[0],mapping1) + " " + x[1] + " " + data);
                    }
                }
                else
                {
                    if (!mapping2.isEmpty())
                    {
                        System.out.println( x[0] + " " + getKey(x[1],mapping2) + " " + data);
                    }
                    else
                    {
                        System.out.println( x[0] + " " + x[1] + " " + data);
                    }
                }

                kdt.add(x, data);
            }
            //System.out.println("Inorder of 2D Kd tree: ");
           // kdt.inorder();
//            Object minX=  Collections.min(valX);
//            Object maxX= Collections.max(valY);
//            Object minY= Collections.min(valY);
//            Object maxY=Collections.max(valY);
////
//            System.out.println(minX +" , "+ maxX);
//            System.out.println(valX+" , "+valY+" ' "+output);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
       // System.out.println("Done");
        System.exit(0);


    }


}

