package com.pi4j.io.gpio.digital;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  DigitalOutputBase.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.exception.IOException;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * <p>Abstract DigitalOutputBase class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public abstract class DigitalOutputBase extends DigitalBase<DigitalOutput, DigitalOutputConfig, DigitalOutputProvider> implements DigitalOutput {

    protected DigitalState state = DigitalState.UNKNOWN;

    /**
     * <p>Constructor for DigitalOutputBase.</p>
     *
     * @param provider a {@link com.pi4j.io.gpio.digital.DigitalOutputProvider} object.
     * @param config a {@link com.pi4j.io.gpio.digital.DigitalOutputConfig} object.
     */
    public DigitalOutputBase(DigitalOutputProvider provider, DigitalOutputConfig config){
        super(provider, config);
    }

    /** {@inheritDoc} */
    @Override
    public DigitalOutput initialize(Context context) throws InitializeException {
        super.initialize(context);

        // update the analog value to the initial value if an initial value was configured
        if(config().initialState() != null){
            try {
                state(config().initialState());
            } catch (IOException e) {
                throw new InitializeException(e);
            }
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public DigitalOutput state(DigitalState state) throws IOException {

        if(!this.state.equals(state)){
            this.state = state;
            this.dispatch(new DigitalStateChangeEvent<DigitalOutputBase>(this, this.state));
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public DigitalOutput pulse(int interval, TimeUnit unit, DigitalState state, Callable<Void> callback) throws IOException {
        int millis = 0;

        // validate arguments
        if(interval <= 0) throw new IllegalArgumentException("A time interval of zero or less is not supported.");
        if(unit == TimeUnit.MICROSECONDS) throw new IllegalArgumentException("TimeUnit.MICROSECONDS is not supported.");
        else if(unit == TimeUnit.DAYS) throw new IllegalArgumentException("TimeUnit.DAYS is not supported.");
        else if(unit == TimeUnit.MILLISECONDS) millis = interval;
        else if(unit == TimeUnit.SECONDS) millis = interval * 1000;
        else if(unit == TimeUnit.MINUTES) millis = interval * 60000;
        else if(unit == TimeUnit.HOURS) millis = interval * 360000;
        else throw new IllegalArgumentException("TimeUnit provided is not supported.");

        // start the pulse state
        this.state(state);

        // block the current thread for the pulse duration
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException("Pulse blocking thread interrupted.", e);
        }

        // end the pulse state
        this.state(DigitalState.getInverseState(state));

        // invoke callback if one was defined
        if (callback != null) {
            try {
                callback.call();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Future<?> pulseAsync(int interval, TimeUnit unit, DigitalState state, Callable<Void> callback) {
        // TODO :: IMPLEMENT DIGITAL OUTPUT PULSE ASYNC
        throw new UnsupportedOperationException("PULSE ASYNC has not yet been implemented!");
    }

    /** {@inheritDoc} */
    /**
     * This method will blink an output pin of the RPi according the given specifications.
     * The pin itself is created while creating a DigitalOutput configuration where one of
     * the parameters is an address (= a BCM pin number).
     *
     * @param delay - The toggle time.
     * @param duration - The amount of times the output has to toggle.
     *
     * Representation:
     *
     *   HIGH +-----+     +-----+     +-----+
     *        |     |     |     |     |     |
     *   LOW  +     +-----+     +-----+     +-----+
     *        ^                                   ^
     * start -┘                                   └- stop
     *         \___/ \___/
     *         delay  delay
     *
     *        \___________________________________/
     *                      duration
     *
     * Example: Delay = 1 sec / duration = 10
     *          Output will be like so (suppose the initial state is ON):
     *          1 - 0 - 1 - 0 - 1 - 0 - 1 - 0 - 1 - 0 with each state lasting for 1 second.
     *          So, if you would connect a LED to the pin, you would see the LED switching
     *          on and off for 5 times, NOT 10 times!!!
     *
     * @param unit - The time unit used to calculate the delay.
     * @param state - The initial state of the pin.
     * @param callback - The method to call, if any, once the blinking is done.
     * @return - The DigitalOutput object itself.
     */
    @Override
    public DigitalOutput blink(int delay, int duration, TimeUnit unit, DigitalState state, Callable<Void> callback) {
        int millis = 0;

        if (delay <= 0) {
            throw new IllegalArgumentException("A delay of zero or less is not supported.");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("A duration of zero or less is not supported.");
        }

        switch (unit) {
            case MICROSECONDS:
                throw new IllegalArgumentException("TimeUnit.MICROSECONDS is not supported.");

            case MILLISECONDS:
                millis = delay;
                break;

            case SECONDS:
                millis = (delay * 1000);
                break;

            case MINUTES:
                millis = (delay * 1000 * 60);
                break;

            case HOURS:
                millis = (delay * 1000 * 60 * 60);
                break;

            default:
                throw new IllegalArgumentException("TimeUnit provided is not supported.");
        }

        this.state(state);

        for (int i = 0; i < duration; i++) {
            // block the current thread for the pulse duration
            try {
                Thread.sleep(millis);
            }
            catch (InterruptedException e) {
                throw new RuntimeException("Pulse blocking thread interrupted. Exception message: [" + e.getMessage() + "].");
            }


            // toggle the pulse state
            this.state(DigitalState.getInverseState(this.state));
        }

        // invoke callback if one was defined
        if (callback != null) {
            try {
                callback.call();
            }
            catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Future<?> blinkAsync(int delay, int duration, TimeUnit unit, DigitalState state, Callable<Void> callback) {
        // TODO :: IMPLEMENT DIGITAL OUTPUT BLINK ASYNC
        throw new UnsupportedOperationException("BLINK ASYNC has not yet been implemented!");
    }

    /** {@inheritDoc} */
    @Override
    public DigitalState state() {
        return this.state;
    }

    /** {@inheritDoc} */
    @Override
    public DigitalOutput shutdown(Context context) throws ShutdownException {
        // set pin state to shutdown state if a shutdown state is configured
        if(config().shutdownState() != null && config().shutdownState() != DigitalState.UNKNOWN){
            try {
                state(config().shutdownState());
            } catch (IOException e) {
                throw new ShutdownException(e);
            }
        }
        return super.shutdown(context);
    }

    /** {@inheritDoc} */
    @Override
    public DigitalOutput on() throws IOException {

        // the default ON state is HIGH
        DigitalState onState = DigitalState.HIGH;

        // get configured ON state
        if(config().onState() != null){
            onState = config().onState();
        }

        // set the current state to the configured ON state
        return state(onState);
    }

    /** {@inheritDoc} */
    @Override
    public DigitalOutput off() throws IOException {
        // the default OFF state is LOW
        DigitalState offState = DigitalState.LOW;

        // get configured ON state; then set OFF state to inverse of ON state
        if(config().onState() != null){
            offState = DigitalState.getInverseState(config().onState());
        }

        // set the current state to the configured OFF state
        return state(offState);
    }
}