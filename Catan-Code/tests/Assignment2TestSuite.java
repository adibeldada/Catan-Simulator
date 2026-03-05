import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

// 1. You MUST import the new classes from the model package
import classes.util.DiceTest;
import classes.util.RuleValidatorTest;
import classes.model.ResourceHandTest;
import classes.model.BoardTest;
import classes.model.CostTest;
import classes.model.BuildingsTest;    
import classes.model.CityTest;        
import classes.model.SettlementTest;

@Suite
@SelectClasses({
    DiceTest.class,
    RuleValidatorTest.class,
    ResourceHandTest.class,
    BoardTest.class,
    CostTest.class,
    BuildingsTest.class,
    CityTest.class,
    SettlementTest.class
})
public class Assignment2TestSuite {
    // Leave empty
}