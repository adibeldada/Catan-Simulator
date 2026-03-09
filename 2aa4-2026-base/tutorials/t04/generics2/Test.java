import java.util.List;
import java.util.ArrayList;

public class Test {

    // public static void scanCargo{...} // implement this

    public static void main(String[] args){
        List<Tshirt> tshirts = new ArrayList<Tshirt>();
        List<Flamethrower> flamethrowers = new ArrayList<Flamethrower>();
        List<Lunchbox> lunchboxes = new ArrayList<Lunchbox>();

        tshirts.add(new Tshirt());
        tshirts.add(new Tshirt());
        tshirts.add(new Tshirt());

        flamethrowers.add(new Flamethrower());

        lunchboxes.add(new Lunchbox());
        lunchboxes.add(new Lunchbox());

        scanCargo(tshirts);
        scanCargo(flamethrowers);
        scanCargo(lunchboxes);
    } 
}