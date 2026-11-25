package br.mainpass.acesso.convite;

import br.mainpass.acesso.convite.dto.ConvidadoDadosRequest;
import br.mainpass.acesso.usuario.Perfil;
import br.mainpass.acesso.usuario.Usuario;
import br.mainpass.acesso.usuario.UsuarioRepository;
import java.security.SecureRandom;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Obtem (ou cadastra) o Usuario CONVIDADO de um convite.
 *
 * Como Usuario.senhaHash e obrigatorio e o projeto ainda nao tem fluxo de primeiro acesso por e-mail,
 * um convidado novo recebe uma senha provisoria aleatoria, devolvida UMA vez ao morador (que a repassa).
 * Troque por link de definicao de senha por e-mail assim que houver envio de e-mail.
 *
 * E o unico ponto que constroi um Usuario.
 */
@Component
public class ConvidadoProvisioner {

    public record Provisionado(Usuario usuario, String senhaProvisoria) {
    }

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public ConvidadoProvisioner(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    public Provisionado obterOuCriar(ConvidadoDadosRequest dados) {
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        String cpf = formatarCpf(dados.cpf());

        var existente = usuarios.findByEmail(email);
        if (existente.isPresent()) {
            Usuario u = existente.get();
            if (u.getPerfil() != Perfil.CONVIDADO) {
                throw ConviteException.regra(HttpStatus.CONFLICT, "EMAIL_EM_USO",
                        "Este e-mail pertence a um usuario que nao e convidado");
            }
            return new Provisionado(u, null);
        }

        if (usuarios.existsByCpf(cpf)) { // adicione `boolean existsByCpf(String cpf)` ao UsuarioRepository (CPF mascarado, varchar(14))
            throw ConviteException.regra(HttpStatus.CONFLICT, "CPF_EM_USO", "CPF ja cadastrado para outro usuario");
        }

        String senha = senhaAleatoria(12);
        Usuario novo = new Usuario(); // Lombok @NoArgsConstructor/@Setter
        novo.setNome(dados.nome().trim().replaceAll("\\s+", " "));
        novo.setEmail(email);
        novo.setCpf(cpf);
        novo.setTelefone(dados.telefone() == null || dados.telefone().isBlank() ? null : dados.telefone());
        novo.setSenhaHash(encoder.encode(senha));
        novo.setPerfil(Perfil.CONVIDADO);
        novo.setAtivo(true);
        return new Provisionado(usuarios.save(novo), senha);
    }

    /** Aceita 11 digitos ou o formato mascarado; devolve 000.000.000-00. Valida os digitos verificadores. */
    static String formatarCpf(String bruto) {
        String d = bruto.replaceAll("\\D", "");
        if (!cpfValido(d)) {
            throw ConviteException.regra(HttpStatus.BAD_REQUEST, "CPF_INVALIDO", "CPF invalido");
        }
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    static boolean cpfValido(String d) {
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return digito(d, 9) == d.charAt(9) - '0' && digito(d, 10) == d.charAt(10) - '0';
    }

    private static int digito(String d, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (d.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static String senhaAleatoria(int tamanho) {
        StringBuilder sb = new StringBuilder(tamanho);
        for (int i = 0; i < tamanho; i++) {
            sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
