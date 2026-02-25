import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

// You MUST import your test classes now because they are in different packages
import classes.util.DiceTest;
import classes.util.RuleValidatorTest;
import classes.model.ResourceHandTest;
import classes.model.BoardTest;
import classes.model.CostTest;

@Suite
@SelectClasses({
    DiceTest.class,
    RuleValidatorTest.class,
    ResourceHandTest.class,
    BoardTest.class,
    CostTest.class
})
public class Assignment2TestSuite {
    // Leave empty
}