package giusa.parser.parameter;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Option annotation in order to specify the name of an option.
 *
 * @author Alessandro Giusa alessandrogiusa@gmail.com
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ METHOD })
public @interface Option {

    /**
     * Get the name of the option.
     * @return name
     */
    String name();
}
