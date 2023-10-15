package br.com.felipeshai.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.felipeshai.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {
    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                var servletPath = request.getServletPath();
                if(servletPath.startsWith("/tasks/")){
                    //Pegar Autenticação
                    var authorization = request.getHeader("Authorization");
    
                    var authEncoded = authorization.substring("Basic".length()).trim();
                    
                    byte[] authDecode = Base64.getDecoder().decode(authEncoded);
                    
                    var authString = new String(authDecode);
    
                    String[] credentials = authString.split(":");
                    String userName = credentials[0];
                    String userPassword = credentials[1];
    
                    System.out.println("Authorization");
                    System.out.println("AuthEncoded: " + authEncoded);
                    System.out.println("AuthDecode: " + authDecode);
                    System.out.println("AuthString: " + authString);
                    System.out.println("Username: " + userName);
                    System.out.println("Password: " + userPassword);
    
                    
                    //Validar Usuario
                    var user = this.userRepository.findByUsername(userName);
                    if(user == null){
                        response.sendError(401);
                    }else{
                        var passwordVerify = BCrypt.verifyer().verify(userPassword.toCharArray(), user.getPassword());
                        if(passwordVerify.verified) {
                            request.setAttribute("idUser", user.getId());
                            filterChain.doFilter(request, response);
                        }else{
                            response.sendError(401);
                        }
                    }

                } else {
                    filterChain.doFilter(request, response);
                }

            }

    
}
