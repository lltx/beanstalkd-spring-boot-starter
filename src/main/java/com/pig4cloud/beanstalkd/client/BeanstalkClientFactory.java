/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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
 */
package com.pig4cloud.beanstalkd.client;

import com.pig4cloud.beanstalkd.client.config.BeanstalkConfigProperties;
import com.pig4cloud.beanstalkd.client.internal.Connection;
import com.pig4cloud.beanstalkd.client.internal.ConnectionInitializer;
import com.pig4cloud.beanstalkd.client.internal.DefaultBeanstalkClient;
import com.pig4cloud.beanstalkd.client.internal.operation.IgnoreOperation;
import com.pig4cloud.beanstalkd.client.internal.operation.UseOperation;
import com.pig4cloud.beanstalkd.client.internal.operation.WatchOperation;
import lombok.AllArgsConstructor;

/**
 * {@link BeanstalkClientFactory} is a factory class, that is responsible for
 * the creation beanstalk client.
 * 
 * @author guojf
 * @version 2.0.0.2013-4-17
 */
@AllArgsConstructor
public class BeanstalkClientFactory {

    private BeanstalkConfigProperties configuration;



    /**
     * create a beanstalk client.
     * 
     * @return
     */
    public BeanstalkClient createBeanstalkClient() {
        return new DefaultBeanstalkClient(configuration, null);
    }

    /**
     * create a job consumer.
     * 
     * @param watchTubes the named tube to the watch list for the current connection
     * @return a beanstalk client
     */
    public JobConsumer createJobConsumer(final String... watchTubes) {
        final boolean ignoreDefault = configuration.getIgnoreDefaultTube();
        ConnectionInitializer initer = connection -> {
            if (watchTubes != null && watchTubes.length > 0) {
                for (int i = 0; i < watchTubes.length; i++) {
                    connection.handle(new WatchOperation(watchTubes[i])).get();
                }
            }

            if (ignoreDefault) {
                connection.handle(new IgnoreOperation("default")).get();
            }
        };
        return new DefaultBeanstalkClient(configuration, initer);
    }

    /**
     * create a job producer.
     * 
     * @param useTube the name of the tube now being used
     * @return a beanstalk client
     */
    public JobProducer createJobProducer(final String useTube) {
        ConnectionInitializer initer = new ConnectionInitializer() {

            @Override
            public void initConnection(Connection connection) throws Exception {
                if (useTube != null) {
                    connection.handle(new UseOperation(useTube));
                }
            }
        };
        return new DefaultBeanstalkClient(configuration, initer);
    }

}
