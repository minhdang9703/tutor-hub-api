package com.tutorhub.common.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * pgjdbc không suy ra được kiểu SQL cho java.time.Instant khi bind tham số ("Can't infer the SQL
 * type"), trong khi đọc cột timestamptz cũng không trả về Instant được. Mọi tham số Instant vì thế
 * được chuyển thành OffsetDateTime UTC ngay tại lớp kết nối, để toàn bộ JdbcTemplate dùng chung một
 * cách xử lý thay vì sửa lẻ tẻ từng câu gọi.
 */
@Configuration
public class InstantParameterConfig {

    @Bean
    static BeanPostProcessor instantParameterPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (!(bean instanceof DataSource ds) || bean instanceof DelegatingDataSource) return bean;
                return new DelegatingDataSource(ds) {
                    @Override
                    public Connection getConnection() throws SQLException {
                        return wrap(super.getConnection());
                    }

                    @Override
                    public Connection getConnection(String username, String password) throws SQLException {
                        return wrap(super.getConnection(username, password));
                    }
                };
            }
        };
    }

    private static Connection wrap(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[] {Connection.class}, new ConnectionHandler(connection));
    }

    private record ConnectionHandler(Connection target) implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (!name.equals("prepareStatement") && !name.equals("prepareCall")) return invokeOn(method, args);
            PreparedStatement statement = (PreparedStatement) invokeOn(method, args);
            return Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                    new Class<?>[] {PreparedStatement.class}, new StatementHandler(statement));
        }

        private Object invokeOn(Method method, Object[] args) throws Throwable {
            try {
                return args == null ? method.invoke(target) : method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    private record StatementHandler(PreparedStatement target) implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("setObject") && args != null && args.length >= 2 && args[0] instanceof Integer
                    && args[1] instanceof Instant instant && !hasExplicitType(method)) {
                args = args.clone();
                args[1] = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
            }
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private boolean hasExplicitType(Method method) {
            Class<?>[] types = method.getParameterTypes();
            return types.length >= 3 && (types[2] == Integer.class || types[2] == int.class);
        }
    }
}
