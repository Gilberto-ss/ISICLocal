/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/WebServices/GenericResource.java to edit this template
 */
package Service;

import java.sql.SQLException;
import java.util.Date;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import modelBD.UsuariosBD;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * REST Web Service
 *
 * @author Gilberto
 */
@Path("Usuarios")
public class UsuariosResource {
private static SessionFactory sessionFactory;
     static {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }
@POST
@Path("guardar")
@Produces(MediaType.APPLICATION_JSON)
public Response guardar(@FormParam("nombre_usuario") String nombre_usuario,
                        @FormParam("contraseña") String contraseña,
                        @FormParam("rol") String rol,
                        @FormParam("correo") String correo,
                        @FormParam("fecha_creacion") Date fecha_creacion,
                        @FormParam("estado") String estado,
                        @FormParam("fecha_ultimo_acceso") Date fecha_ultimo_acceso,
                        @FormParam("activo") int activo) {
    Session session = null;
    Transaction transaction = null;

    if (nombre_usuario == null || nombre_usuario.trim().isEmpty() ||
        contraseña == null || contraseña.trim().isEmpty() ||
        rol == null || correo == null || correo.trim().isEmpty() ||
        fecha_creacion == null ||
        estado == null || estado.trim().isEmpty() ||
        fecha_ultimo_acceso == null) {

        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("{\"error\":\"Todos los campos son obligatorios.\"}")
                       .build();
    }

    try {
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();

        UsuariosBD usuario = new UsuariosBD();
        usuario.setNombre_usuario(nombre_usuario);
        usuario.setContraseña(contraseña);
        usuario.setRol(rol);
        usuario.setCorreo(correo);
        usuario.setFecha_creacion(fecha_creacion);
        usuario.setEstado(estado);
        usuario.setFecha_ultimo_acceso(fecha_ultimo_acceso);

        session.save(usuario);
        transaction.commit();

        return Response.ok("{\"message\":\"Usuario guardado exitosamente\"}")
                       .build();
    } catch(Exception e){
     if (transaction != null) transaction.rollback();
        e.printStackTrace();
    return Response.ok("{\"message\":\"no se pudo guardar el Usuario\"}")
                       .build();
    } finally {
        if (session != null) session.close();
    }

    
    }
    @Path("eliminar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(@QueryParam("id") int id) {
        Session session = null;
        Transaction transaction = null;

        if (id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"El campo 'id' es obligatorio y debe ser mayor a cero.\"}")
                           .build();
        }

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            UsuariosBD usuario = session.get(UsuariosBD.class, id);

            if (usuario == null) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"error\":\"Usuario no encontrado.\"}")
                               .build();
            }

            usuario.setActivo(false);
            session.update(usuario);
            transaction.commit();

            return Response.ok("{\"message\":\"Usuario desactivado exitosamente\"}")
                           .build();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\":\"Error al desactivar el usuario\"}")
                           .build();
        } finally {
            if (session != null) session.close();
        }
    }

}
