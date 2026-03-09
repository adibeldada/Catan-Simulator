public class Test {
    public static void main(String[] args){
        System.out.println("hello");

        SpaceballSpeedRegister<String> stringBasedSpeedRegister = new SpaceballSpeedRegister<String>();
        stringBasedSpeedRegister.set("ludicrous");
        System.out.println(stringBasedSpeedRegister.get());

        SpaceballSpeedRegister<Double> doubleBasedSpeedRegister = new SpaceballSpeedRegister<Double>();
        doubleBasedSpeedRegister.set(288.4);
        System.out.println(doubleBasedSpeedRegister.get());
    } 
}