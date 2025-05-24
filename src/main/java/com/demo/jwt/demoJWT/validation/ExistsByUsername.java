package com.demo.jwt.demoJWT.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ExistsByUsernameValidation.class) //Le dice a Spring que esta anotación (@ExistsByUsername) usa la clase ExistsByUsernameValidation para hacer la lógica de validación real
@Target(ElementType.FIELD) //FIELD: Indica que esta anotación solo se puede poner en campos (por ejemplo, atributos de una clase DTO o Entity).
@Retention(RetentionPolicy.RUNTIME) //Significa que la anotación estará disponible en tiempo de ejecución, para que Spring pueda usarla cuando valide el objeto
public @interface ExistsByUsername { //@interface indica que ExistsByUsername es una anotacion personalizada de java
    String message() default "Ya existe usuario en la Base de datos"; // mensaje de error que se mostrará si la validación falla

    Class<?>[] groups() default {}; //Son grupos de validación, útil si usas validaciones agrupadas (por ejemplo, validar algunas cosas solo en ciertos contextos). Normalmente lo dejas vacío.

    Class<? extends Payload>[] payload() default {}; //Sirve para agregar metadata adicional si quieres. También casi siempre lo dejas vacío, a menos que necesites algo muy especial
}
