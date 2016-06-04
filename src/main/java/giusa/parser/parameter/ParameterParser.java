/***************************************************************************
 * Copyright 2016 GiusaSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package giusa.parser.parameter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This parser will parse arguments passed by the command line. It can parse
 *
 * <ul>
 * <li>unnamed arguments sorted by position, where the named arguments do not
 * interfere the position value</li>
 * <li>named arguments --name=value or --name value or -name=value or -name
 * value</li>
 * <li></li>
 *
 * </ul>
 *
 * @author Alessandro Giusa, alessandrogiusa@gmail.com
 * @version 1.0
 *
 */
public final class ParameterParser {

    /** Delimiter $. */
    private static final String PREFIX_UNNAMED_PARAMETER = "$";
    /** Delimiter --. */
    private static final String PREFIX_PARAMETER_DOUBLE_DASH = "--";
    /** Delimiter -. */
    private static final String PREFIX_PARAMETER_SINGEL_DASH = "-";
    /** Delimiter =. */
    private static final String SEPARATOR_KEY_VALUE = "=";
    /** Delimiter '. */
    private static final String SINGLE_QUOATS = "'";
    /** Delimiter ". */
    private static final String DOUBLE_QUOATS = "\"";

    // *****************************************************************
    // AVAILABLE STATES
    // *****************************************************************

    /** Init state, mostly dispatching. */
    static final ParameterParserState INIT_STATE = new InitState();
    /** state to parse unnamed parameter. */
    static final ParameterParserState UNNAMED_PARAMETER_STATE =
            new UnnamedParameterState();
    /** state to parse named parameter starting with double dash. */
    static final ParameterParserState NAMED_DOUBLE_DASH_PARAMETER_STATE =
            new DashParameterState(
                    ParameterParser.PREFIX_PARAMETER_DOUBLE_DASH);
    /** state to parse named parameter starting with single dash. */
    static final ParameterParserState NAMED_SINGLE_DASH_PARAMETER_STATE =
            new DashParameterState(
                    ParameterParser.PREFIX_PARAMETER_SINGEL_DASH);

    // ********************************************************************
    // *
    // ********************************************************************

    /**
     * Parameter map. Contains named and unnamed parameter unnamed start with
     * {@link #PREFIX_UNNAMED_PARAMETER}.
     */
    private final Map<String, String> parameter;

    /**
     * options.
     */
    private final Set<String> options;

    /** current state. */
    private ParameterParserState state = ParameterParser.INIT_STATE;

    /** counter of unnamed parameter. */
    private int counterUnnamedParameter = 0;

    /**switch to set that options have to be parsed.*/
    private boolean parseOptions = true;

    /**
     * Create a ParameterParser object.
     */
    public ParameterParser() {
        this.parameter = new HashMap<String, String>();
        this.options = new HashSet<String>();
    }

    /**
     * Get the number of unnamed parameter.
     *
     * @return number of counted parameters
     */
    public int getCountUnnamedParameter() {
        return this.counterUnnamedParameter;
    }

    /**
     * Get the state of options being parsed.
     * @return true if options are parsed
     */
    public boolean isParseOptions() {
        return this.parseOptions;
    }

    /**
     * Set the state of options being parsed.
     * @param doParseOptions true if options have to be parsed
     */
    public void setParseOptions(final boolean doParseOptions) {
        this.parseOptions = doParseOptions;
    }

    /**
     * Parse the given arguments.
     *
     * @param args
     *            arguments to parse
     */
    public void parse(final String[] args) {
        if (args != null && args.length > 0) {
            for (int i = 0, len = args.length; i < len;) {
                final String next = args[i];
                final int retValue = this.state.run(this, next.trim());
                switch (retValue) {
                case ParameterParserState.NEXT_TOKEN:
                    i++;
                    break;
                case ParameterParserState.SAME_TOKEN:
                    break;
                case ParameterParserState.STOP:
                    return; // stop the loop
                default:
                    break;
                }
            }
        }
    }

    /**
     * Set the next state.
     *
     * @param nextState
     *            state to set next
     */
    void setState(final ParameterParserState nextState) {
        this.state = nextState;
    }

    /**
     * Get the internal map of parameter.
     *
     * @return map of parameters
     */
    Map<String, String> getParameters() {
        return this.parameter;
    }

    /**
     * Get the internal set of options.
     * @return set of options
     */
    Set<String> getOptions() {
        return this.options;
    }

    /**
     * Add an unnamed parameter.
     *
     * @param value
     *            parameter to add
     */
    void addUnnamedParameter(final String value) {
        final String key = ParameterParser.PREFIX_UNNAMED_PARAMETER
                + String.valueOf(this.counterUnnamedParameter++);
        this.parameter.put(key, value);
    }

    /**
     * Add a named parameter.
     *
     * @param key
     *            name of the parameter
     * @param value
     *            value of the parameter
     */
    void addNamedParameter(final String key, final String value) {
        if (this.parameter.containsKey(key)) {
            throw new IllegalArgumentException(
                    String.format("parameter %s already set", key));
        }
        this.parameter.put(key, value);
    }

    /**
     * Add an option.
     * @param name name of option
     */
    void addOption(final String name) {
        this.options.add(name);
    }

    // *****************************************************************
    // CONVENIENT GETTER
    // *****************************************************************

    /**
     * Check if there is an option. Pass also the dash i.e. -f or --names or
     * something.
     * @param name name with the dash symbol
     * @return true if the option is available
     */
    public boolean hasOption(final String name) {
        return this.options.contains(name);
    }

    /**
     * Get the parameter either by the name or by its position.
     *
     * @param name
     *            name of the parameter. Do not use dash or other, just the name
     * @param pos
     *            position like 0 or 1 or other integer.
     * @param required
     *            true if the parameter is absolute required. If true and
     *            parameter does not exist, the runtime exception below is
     *            thrown. If false and parameter does not exist <b>null</b> is
     *            returned.
     * @throws MissingParameterException
     *             if parameter required and not existent
     * @return the value of the parameter
     */
    public String getParameterString(final String name, final int pos,
            final boolean required) throws MissingParameterException {
        final String value;
        if (this.contains(name)) {
            value = this.getNamedString(name);
        } else if (this.contains(pos)) {
            value = this.getUnnamedString(pos);
            System.out.printf("read argument %s from %s.slot",
                    name, String.valueOf(pos));
        } else {
            if (required) {
            	final String errorMessage = this.getErrorMessageMissingParameter(name, pos);
                throw new MissingParameterException(name, pos, errorMessage);
            }
            value = null;
        }
        return value;
    }

    /**
     * Get the parameter either by the name or by its position.
     *
     * @param name
     *            name of the parameter. Do not use dash or other, just the name
     * @param pos
     *            position like 0 or 1 or other integer.
     * @param required
     *            true if the parameter is absolute required. If true and
     *            parameter does not exist, the runtime exception below is
     *            thrown. If false and parameter does not exist <b>0</b> is
     *            returned.
     * @throws MissingParameterException
     *             if parameter required and not existent
     * @return the value of the parameter
     */
    public int getParameterInt(final String name, final int pos,
            final boolean required) throws MissingParameterException {
        final int value;
        if (this.contains(name)) {
            value = this.getNamedInt(name);
        } else if (this.contains(pos)) {
            value = this.getUnnamedInt(pos);
            System.out.printf("read argument %s from %s.slot",
                    name, String.valueOf(pos));
        } else {
            if (required) {
            	final String errorMessage = this.getErrorMessageMissingParameter(name, pos);
                throw new MissingParameterException(name, pos, errorMessage);
            }
            value = 0;
        }
        return value;
    }

    /**
     * Get the parameter either by the name or by its position.
     *
     * @param name
     *            name of the parameter. Do not use dash or other, just the name
     * @param pos
     *            position like 0 or 1 or other integer.
     * @param required
     *            true if the parameter is absolute required. If true and
     *            parameter does not exist, the runtime exception below is
     *            thrown. If false and parameter does not exist <b>0</b> is
     *            returned.
     * @throws MissingParameterException
     *             if parameter required and not existent
     * @return the value of the parameter
     */
    public long getParameterLong(final String name, final int pos,
            final boolean required) throws MissingParameterException {
        final long value;
        if (this.contains(name)) {
            value = this.getNamedLong(name);
        } else if (this.contains(pos)) {
            value = this.getUnnamedLong(pos);
            System.out.printf("read argument %s from %s.slot",
                    name, String.valueOf(pos));
        } else {
            if (required) {
            	final String errorMessage = this.getErrorMessageMissingParameter(name, pos);
                throw new MissingParameterException(name, pos, errorMessage);
            }
            value = 0L;
        }
        return value;
    }

    /**
     * Get the parameter either by the name or by its position.
     *
     * @param name
     *            name of the parameter. Do not use dash or other, just the name
     * @param pos
     *            position like 0 or 1 or other integer.
     * @param required
     *            true if the parameter is absolute required. If true and
     *            parameter does not exist, the runtime exception below is
     *            thrown. If false and parameter does not exist <b>0</b> is
     *            returned.
     * @throws MissingParameterException
     *             if parameter required and not existent
     * @return the value of the parameter
     */
    public float getParameterFloat(final String name, final int pos,
            final boolean required) throws MissingParameterException {
        final float value;
        if (this.contains(name)) {
            value = this.getNamedFloat(name);
        } else if (this.contains(pos)) {
            value = this.getUnnamedFloat(pos);
            System.out.printf("read argument %s from %s.slot",
                    name, String.valueOf(pos));
        } else {
            if (required) {
            	final String errorMessage = this.getErrorMessageMissingParameter(name, pos);
                throw new MissingParameterException(name, pos, errorMessage);
            }
            value = 0f;
        }
        return value;
    }

    /**
     * Get the parameter either by the name or by its position.
     *
     * @param name
     *            name of the parameter. Do not use dash or other, just the name
     * @param pos
     *            position like 0 or 1 or other integer.
     * @param required
     *            true if the parameter is absolute required. If true and
     *            parameter does not exist, the runtime exception below is
     *            thrown. If false and parameter does not exist <b>0</b> is
     *            returned.
     * @throws MissingParameterException
     *             if parameter required and not existent
     * @return the value of the parameter
     */
    public double getParameterDouble(final String name, final int pos,
            final boolean required) throws MissingParameterException {
        final double value;
        if (this.contains(name)) {
            value = this.getNamedDouble(name);
        } else if (this.contains(pos)) {
            value = this.getUnnamedDouble(pos);
            System.out.printf("read argument %s from %s.slot",
                    name, String.valueOf(pos));
        } else {
            if (required) {
                final String errorMessage = this.getErrorMessageMissingParameter(name, pos);
                throw new MissingParameterException(name, pos, errorMessage);
            }
            value = 0d;
        }
        return value;
    }
    
    /**
     * Create the error message for missing arguments.
     * @param name name of argument
     * @param expectedPosition expected position
     * @return string with error message
     */
    private String getErrorMessageMissingParameter(final String name, final int expectedPosition) {
    	final String errorMessage;
    	if (expectedPosition < 0) {
    		errorMessage = String.format(
                    "required argument %s not does not exist!",
                            name);
    	} else {
    		errorMessage = String.format(
                    "required argument %s does neither exist as "
                            + "named argument nor in position %s.",
                            name,
                            String.valueOf(expectedPosition));
    	}
    	return errorMessage;
    }

    // *****************************************************************
    // MORE CONVENIENT METHOD TO EXTRACT PARAMETERS
    // *****************************************************************

    /**
     * Get the parameter the {@link ParameterBean} object. The values are read
     * by reflection and set to the given bean by the provided setter/getter
     * methods. The bean must obey the Java bean conventions.
     *
     * @param bean
     *            object to store values
     * @throws MissingParameterException
     *             if some required parameter is missing
     */
    public void getParameter(final ParameterBean bean)
            throws MissingParameterException {
        final Method[] methods = bean.getClass().getDeclaredMethods();
        for (int i = 0, len = methods.length; i < len; i++) {
            final Method method = methods[i];
            if (method.isAnnotationPresent(Parameter.class)) {
                this.processParameterAnnotation(method, bean);
            } else if (method.isAnnotationPresent(Option.class)) {
                this.processOptionAnnotation(method, bean);
            }
        }
    }

    /**
     * Helper to process the {@link Option} annotation type.
     *
     * @param method
     *            method
     * @param bean
     *            bean
     * @throws IllegalArgumentException
     *             in case the getter is has not boolean as return type.
     */
    private void processOptionAnnotation(final Method method,
            final ParameterBean bean) {
        // check the return type
        final Class<?> returnType = method.getReturnType();
        if (returnType != boolean.class) {
            final String errMsg = String.format("wrong return "
                    + "type on %s. It should be boolean type",
                    method.getName());
            throw new IllegalArgumentException(errMsg);
        }
        final String methodName = method.getName();
        final String prefixMethod = "is";

        // construct the setter name
        final String setterMethodName = "set"
                + methodName.substring(methodName.indexOf(prefixMethod)
                        + prefixMethod.length(), methodName.length());

        // get the setter method
        final Method setterMethod = this.getMethodByReflection(bean,
                setterMethodName, returnType);

        // fetch details from annotation
        final Option annotation = method.getAnnotation(Option.class);
        final String optionName = annotation.name();
        final boolean optionIsPresent = this.hasOption(optionName);
        try {
            setterMethod.invoke(bean, Boolean.valueOf(optionIsPresent));
        } catch (IllegalAccessException e) {
            final String errMsg = String.format(
                    "Tried to invoke %s by reflection "
                            + "but got IllegalAccessException",
                            setterMethodName);
            System.err.println(errMsg);
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            final String errMsg = String.format(
                    "Tried to invoke %s by reflection "
                            + "but got IllegalArgumentException,"
                            + " seems that the return type of getter "
                            + "and setter do not match.",
                    setterMethodName);
            System.err.println(errMsg);
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            final String errMsg = String.format(
                    "Tried to invoke %s by reflection "
                            + "but got InvocationTargetException",
                    setterMethodName);
            System.err.println(errMsg);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Helper to process the {@link Parameter} annotation type.
     * @param method getter method
     * @param bean bean
     * @throws MissingParameterException if required but not existent
     */
    private void processParameterAnnotation(final Method method,
            final ParameterBean bean) throws MissingParameterException {
        final Class<?> returnType = method.getReturnType();
        // get the method name and retrieve the corresponding setter
        final String methodName = method.getName();
        if (returnType != boolean.class
                && !methodName.startsWith("get")) {
            final String errMsg = String.format("you put %s annotation "
                    + "on method which is not a getter: %s."
                    + " Use this annotation on getter since the "
                    + "name will be extracted "
                    + "to get the corresponding setter."
                    + " (JavaBean convention)",
                    Parameter.class.getSimpleName(), methodName);
            throw new IllegalArgumentException(errMsg);
        }
        // check the prefix of the 'getter' method
        final String prefixMethod;
        if (returnType == boolean.class) {
            prefixMethod = "is";
        } else {
            prefixMethod = "get";
        }

        // construct the setter name
        final String setterMethodName = "set"
                + methodName.substring(methodName.indexOf(prefixMethod)
                        + prefixMethod.length(), methodName.length());

        // get the setter method
        final Method setterMethod = this.getMethodByReflection(bean,
                setterMethodName, returnType);

        // retrieve parameter informations
        final Parameter annotation = method
                .getAnnotation(Parameter.class);
        final String argName = annotation.name();
        final int position = annotation.position();
        final boolean required = annotation.required();

        if (!required && position >= 0) {
            final String errMsg = String.format("the method %s has "
                    + "an parameter %s which is not required but has a position"
                    + " attached. This is illegal state, since a not required"
                    + " parameter can not be parsed over position.",
                            methodName, argName);
            throw new IllegalStateException(errMsg);
        }

        try {
            if (returnType == String.class) {
                final String value = this.getParameterString(
                        argName, position, required);
                setterMethod.invoke(bean, value);
                
            } else if (returnType == int.class) {
                final int value = this.getParameterInt(
                        argName, position, required);
                setterMethod.invoke(bean, Integer.valueOf(value));

            } else if (returnType == long.class) {
                final long value = this.getParameterLong(
                        argName, position, required);
                setterMethod.invoke(bean, Long.valueOf(value));

            } else if (returnType == float.class) {
                final float value = this.getParameterFloat(
                        argName, position, required);
                setterMethod.invoke(bean, Float.valueOf(value));

            } else if (returnType == double.class) {
                final double value = this.getParameterDouble(
                        argName, position, required);
                setterMethod.invoke(bean, Double.valueOf(value));
            }

        } catch (IllegalAccessException e) {
            final String errMsg = String.format(
                    "Tried to invoke %s by reflection "
                            + "but got IllegalAccessException",
                            setterMethodName);
            System.err.println(errMsg);
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            final String errMsg = String.format(
                    "Tried to invoke %s by reflection "
                            + "but got IllegalArgumentException,"
                            + " seems that the return type of getter "
                            + "and setter do not match.",
                    setterMethodName);
            System.err.println(errMsg);
            e.printStackTrace();
            System.exit(1);
        } catch (InvocationTargetException e) {
            final String errMsg = String.format(
                    "Tried to invoke %s by reflection "
                            + "but got InvocationTargetException",
                    setterMethodName);
            System.err.println(errMsg);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Helper to fetch a method by reflection.<br>
     * If the method can not be fetched the program will print an error message
     * an stop.
     *
     * @param object
     *            object to search on
     * @param name
     *            name of the method
     * @param parameterType
     *            parameter type
     * @return the method
     */
    private Method getMethodByReflection(final Object object, final String name,
            final Class<?> parameterType) {
        // get the setter method
        Method method = null;
        try {
            method = object.getClass()
                    .getMethod(name, parameterType);
        } catch (NoSuchMethodException e) {
            final String errMsg = String.format(
                    "Expected Setter method %s missing."
                          + "\nPlease implement for each getter the "
                          + "corresponding setter!(JavaBeanConvention)",
                          name);
            System.err.println(errMsg);
            e.printStackTrace();
            System.exit(1);
        } catch (SecurityException e) {
            System.err.println("Tried to get the method by reflection "
                    + "and got missing access permission!");
            e.printStackTrace();
            System.exit(1);
        }

        return method;
    }

    // ********************************************************************
    // * CHECK
    // ********************************************************************

    /**
     * Check whether or not the given argument was passed.
     *
     * @param key
     *            name of the argument
     * @return true if present
     */
    public boolean contains(final String key) {
        return this.parameter.containsKey(key);
    }

    /**
     * Check whether or not an argument on the given position is present. Named
     * arguments will not interfere with the position number.
     *
     * @param pos
     *            position starting by 0
     * @return true if present
     */
    public boolean contains(final int pos) {
    	if (pos < 0) {
			return false;
		}
        final String key = ParameterParser.PREFIX_UNNAMED_PARAMETER + pos;
        return this.contains(key);
    }

    // ********************************************************************
    // * GETTER NAMED
    // ********************************************************************

    /**
     * Get the long value of the given argument name.
     *
     * @param key
     *            argument name
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not a long
     */
    public long getNamedLong(final String key) {
        if (!this.parameter.containsKey(key)) {
            throw new IllegalArgumentException(String.format(
                    "argument with name %s not passed", key));
        }
        return Long.parseLong(this.parameter.get(key));
    }

    /**
     * Get the int value of the given argument name.
     *
     * @param key
     *            argument name
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not an int
     */
    public int getNamedInt(final String key) {
        if (!this.parameter.containsKey(key)) {
            throw new IllegalArgumentException(String.format(
                    "argument with name %s not passed", key));
        }
        return Integer.parseInt(this.parameter.get(key));
    }

    /**
     * Get the double value of the given argument name.
     *
     * @param key
     *            argument name
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not a double
     */
    public double getNamedDouble(final String key) {
        if (!this.parameter.containsKey(key)) {
            throw new IllegalArgumentException(String.format(
                    "argument with name %s not passed", key));
        }
        return Double.parseDouble(this.parameter.get(key));
    }

    /**
     * Get the float value of the given argument name.
     *
     * @param key
     *            argument name
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not a float
     */
    public float getNamedFloat(final String key) {
        if (!this.parameter.containsKey(key)) {
            throw new IllegalArgumentException(String.format(
                    "argument with name %s not passed", key));
        }
        return Float.parseFloat(this.parameter.get(key));
    }

    /**
     * Get the boolean value of the given argument name.
     *
     * @param key
     *            argument name
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not a boolean
     */
    public boolean getNamedBoolean(final String key) {
        if (!this.parameter.containsKey(key)) {
            throw new IllegalArgumentException(String.format(
                    "argument with name %s not passed", key));
        }
        return Boolean.parseBoolean(this.parameter.get(key));
    }

    /**
     * Get the String value of the given argument name.
     *
     * @param key
     *            argument name
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not a string
     */
    public String getNamedString(final String key) {
        if (!this.parameter.containsKey(key)) {
            throw new IllegalArgumentException(String.format(
                    "argument with name %s not passed", key));
        }
        return this.parameter.get(key);
    }

    // ********************************************************************
    // * GETER UNNAMED
    // ********************************************************************

    /**
     * Get the long value of the given argument position. Named arguments will
     * not interfere with the position number
     *
     * @param pos
     *            position starting by 0
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not a long
     */
    public long getUnnamedLong(final int pos) {
        final String key = ParameterParser.PREFIX_UNNAMED_PARAMETER + pos;
        return this.getNamedLong(key);
    }

    /**
     * Get the int value of the given argument position. Named arguments will
     * not interfere with the position number
     *
     * @param pos
     *            position starting by 0
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not an int
     */
    public int getUnnamedInt(final int pos) {
        final String key = ParameterParser.PREFIX_UNNAMED_PARAMETER + pos;
        return this.getNamedInt(key);
    }

    /**
     * Get the double value of the given argument position. Named arguments will
     * not interfere with the position number
     *
     * @param pos
     *            position starting by 0
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not a double
     */
    public double getUnnamedDouble(final int pos) {
        final String key = ParameterParser.PREFIX_UNNAMED_PARAMETER + pos;
        return this.getNamedDouble(key);
    }

    /**
     * Get the float value of the given argument position. Named arguments will
     * not interfere with the position number
     *
     * @param pos
     *            position starting by 0
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not a float
     */
    public float getUnnamedFloat(final int pos) {
        final String key = ParameterParser.PREFIX_UNNAMED_PARAMETER + pos;
        return this.getNamedFloat(key);
    }

    /**
     * Get the boolean value of the given argument position. Named arguments
     * will not interfere with the position number
     *
     * @param pos
     *            position starting by 0
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not a boolean
     */
    public boolean getUnnamedBoolean(final int pos) {
        final String key = ParameterParser.PREFIX_UNNAMED_PARAMETER + pos;
        return this.getNamedBoolean(key);
    }

    /**
     * Get the String value of the given argument position. Named arguments will
     * not interfere with the position number
     *
     * @param pos
     *            position starting by 0
     * @return value value of parameter
     * @throws IllegalArgumentException
     *             in case the value is not a String
     */
    public String getUnnamedString(final int pos) {
        final String key = ParameterParser.PREFIX_UNNAMED_PARAMETER + pos;
        return this.getNamedString(key);
    }

    // ********************************************************************
    // * STATES AND HELPER
    // ********************************************************************

    /**
     * Helper to remove quotas.
     * @param string string remove quotas from
     * @return String with removed quotas
     */
    static String removeQuotas(final String string) {

        if (string.contains(ParameterParser.DOUBLE_QUOATS)) {
            return string.substring(1, string.length() - 1);

        } else if (string.contains(ParameterParser.SINGLE_QUOATS)) {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }

    /**
     * State of parsing process.
     *
     * @author Alessandro Giusa, alessandrogiusa@gmail.com
     * @version 1.0
     *
     */
    private interface ParameterParserState {

        /** option to get next token. */
        int NEXT_TOKEN = 0;
        /** option to get same token. */
        int SAME_TOKEN = 1;
        /** option to stop. */
        int STOP = 2;

        /**
         * Run the state.
         *
         * @param parser
         *            parser instance
         * @param next
         *            next token to parse
         * @return value to tell internal loop what to do next see
         *         {@link #NEXT_TOKEN}, {@link #SAME_TOKEN},{@link #STOP}
         */
        int run(ParameterParser parser, String next);
    }

    // ********************************************************************
    // * STATES IMPL.
    // ********************************************************************

    /**
     * The basic dispatcher state.
     *
     * @author Alessandro Giusa, alessandrogiusa@gmail.com
     * @version 1.0
     *
     */
    static class InitState implements ParameterParserState {
        @Override
        public int run(final ParameterParser parser, final String next) {
            int retVal = ParameterParserState.SAME_TOKEN;
            if (next.startsWith(ParameterParser.PREFIX_PARAMETER_DOUBLE_DASH)) {
                parser.setState(ParameterParser
                        .NAMED_DOUBLE_DASH_PARAMETER_STATE);

            } else if (next.startsWith(ParameterParser
                    .PREFIX_PARAMETER_SINGEL_DASH)) {
                parser.setState(ParameterParser
                        .NAMED_SINGLE_DASH_PARAMETER_STATE);

            } else {
                parser.setState(ParameterParser.UNNAMED_PARAMETER_STATE);
            }
            return retVal;
        }
    }

    /**
     * State which handles unnamed args but in order they were typed in.
     *
     * @author Alessandro Giusa, alessandrogiusa@gmail.com
     * @version 1.0
     *
     */
    static class UnnamedParameterState implements ParameterParserState {

        @Override
        public int run(final ParameterParser parser, final String next) {
            parser.addUnnamedParameter(ParameterParser.removeQuotas(next));
            parser.setState(ParameterParser.INIT_STATE);
            return ParameterParserState.NEXT_TOKEN;
        }
    }

    /**
     * State which handles dash named arguments.
     *
     * @author Alessandro Giusa, alessandrogiusa@gmail.com
     * @version 1.0
     *
     */
    static class DashParameterState implements ParameterParserState {

        /**the dash type used in this state.*/
        private final String dashType;
        /**name of parameter.*/
        private String name;
        /**value of parameter.*/
        private String value;

        /**
         * Create {@link DashParameterState} object with the given dash type.
         *
         * @param dash
         *            dash type
         */
        DashParameterState(final String dash) {
            this.dashType = dash;
        }

        @Override
        public int run(final ParameterParser parser, final String next) {
            int retVal = ParameterParserState.NEXT_TOKEN;
            if (this.name == null) {
                if (next.contains(ParameterParser.SEPARATOR_KEY_VALUE)) {
                    this.name = next.substring(next.indexOf(this.dashType)
                            + this.dashType.length(),
                            next.indexOf(ParameterParser.SEPARATOR_KEY_VALUE));
                    this.value = next.substring(next.indexOf(
                            ParameterParser.SEPARATOR_KEY_VALUE)
                            + ParameterParser.SEPARATOR_KEY_VALUE.length(),
                            next.length());
                    this.value = ParameterParser.removeQuotas(this.value);
                    this.flushParameter(parser);
                } else {
                    if (parser.isParseOptions()) {
                        this.name = next;
                        this.flushOption(parser);
                    } else {
                        this.name = next.substring(next.indexOf(this.dashType)
                                + this.dashType.length(), next.length());
                    }
                }

            } else {
                this.value = next;
                this.value = ParameterParser.removeQuotas(this.value);
                this.flushParameter(parser);
            }
            return retVal;
        }

        /**
         * Flush name the captured option.
         * @param parser parser instance
         */
        private void flushOption(final ParameterParser parser) {
            parser.addOption(this.name);
            parser.setState(ParameterParser.INIT_STATE);
            this.name = null;
            this.value = null;
        }

        /**
         * Flush the key, value to the parser.
         *
         * @param parser
         *            parser instance
         */
        private void flushParameter(final ParameterParser parser) {
            parser.addNamedParameter(this.name, this.value);
            parser.setState(ParameterParser.INIT_STATE);
            this.name = null;
            this.value = null;
        }
    }
}
