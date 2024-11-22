//TODO: apagar histórico de transações de contas deletadas
//TODO: tabela sessões?
//TODO: validação de login (numero e senha)
//TODO: conta gerente
//TODO: pesquisa de conta por nome/numero (para transferencia)

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO {
	// Tipos de transação
	public static final int TR_DEPOSITO = 0;
	public static final int TR_SAQUE = 1;
	public static final int TR_TRANSFERENCIA = 2;

	private static Connection conectar() throws SQLException {
		Connection conn = null;

		conn = DriverManager.getConnection("jdbc:sqlite:database.db");

		return conn;
	}

	/*
	public static boolean validarSessao(int numeroConta, String token) throws SQLException {
		try (Connection conn = conectar()) {
			if (conn == null) return false;

			return true;
		}
	}
	*/

	public static void criarTabelas() throws SQLException {
		try (Connection conn = conectar()) {
			if (conn == null) return;

			// Tabela com as contas
			String sql =   "CREATE TABLE IF NOT EXISTS conta_corrente ("
						 + "numero_conta INTEGER PRIMARY KEY, "
						 + "nome VARCHAR(60), "
						 + "saldo REAL)";

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sql);
			}

			// Tabela com os registros de transações
			sql =   "CREATE TABLE IF NOT EXISTS transacoes ("
				  + "id_transacao INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + "tipo_transacao INTEGER, "
				  + "quantia REAL, "
				  + "id_origem INTEGER REFERENCES conta_corrente(numero_conta), "
				  + "id_destino INTEGER REFERENCES conta_corrente(numero_conta), "
				  + "data_hora DATETIME)";

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sql);
			}

			/*
			// Tabela com sessões de usuário
			sql =   "CREATE TABLE IF NOT EXISTS sessoes ("
				  + "id_sessao INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + "token CHAR(20) UNIQUE, "
				  + "numero_conta INTEGER REFERENCES conta_corrente(numero_conta), "
				  + "data_expiracao DATETIME)"

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sql);
			}
			*/
		}
	}

	public static void novaConta(int numeroConta, String nome) throws SQLException {
		try (Connection conn = conectar()) {
			if (conn == null) return;
			if (numeroConta <= 0) throw new IllegalArgumentException("O número da conta deve ser positivo.");
			if (nome.length() < 3 || nome.length() > 60) throw new IllegalArgumentException("Nome deve possuir entre 3 e 60 caracteres.");

			Conta conta = buscarConta(numeroConta);
			if (conta != null) throw new IllegalArgumentException("Uma conta com este número já existe.");

			String sql =   "INSERT INTO conta_corrente "
			             + "VALUES (?, ?, 0)";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, numeroConta);
				pstmt.setString(2, nome);
	
				pstmt.execute();
			}
		}
	}

	public static Conta buscarConta(int numeroConta) throws SQLException {
		Conta conta = null;

		try (Connection conn = conectar()) {
			if (conn == null) return conta;
			if (numeroConta <= 0) throw new IllegalArgumentException("O número da conta deve ser positivo.");

			String sql =   "SELECT numero_conta, nome, saldo " 
			             + "FROM conta_corrente "
			             + "WHERE numero_conta = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, numeroConta);
	
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					conta = new Conta(rs.getInt("numero_conta"), rs.getString("nome"), rs.getDouble("saldo"));
				}
			}
		}

		return conta;
	}

	public static List<Historico> buscarHistorico(int numeroConta) throws SQLException {
		List<Historico> listaHistorico = new ArrayList<>();
		
		try (Connection conn = conectar()) {
			if (conn == null) return null;
			if (numeroConta <= 0) throw new IllegalArgumentException("O número da conta deve ser positivo.");

			Conta conta = buscarConta(numeroConta);
			if (conta == null) throw new IllegalArgumentException("Número de conta inválido.");

			String sql =   "SELECT * "
			             + "FROM transacoes "
			             + "WHERE id_origem = ? OR id_destino = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, numeroConta);
				pstmt.setInt(2, numeroConta);

				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					Historico historico = new Historico(
						rs.getInt("id_transacao"),
						rs.getString("tipo_transacao"),
						rs.getDouble("quantia"),
						rs.getInt("id_origem"),
						rs.getInt("id_destino"),
						rs.getObject("data_hora", LocalDateTime.class)
					);

					listaHistorico.add(historico);
				}
			}

			return listaHistorico;
		}
	}

    public static void executarTransacao(int tipoTransacao, double quantia, int numeroOrigem, int numeroDestino) throws SQLException, IllegalArgumentException {
    	try (Connection conn = conectar()) {
    		if (conn == null) return;
        	if (quantia <= 0) throw new IllegalArgumentException("O valor para transação deve ser positivo.");
			if (numeroOrigem <= 0) throw new IllegalArgumentException("O número da conta de origem deve ser positivo.");
			if (numeroDestino <= 0) throw new IllegalArgumentException("O número da conta de destino deve ser positivo.");

			Conta contaOrigem = buscarConta(numeroOrigem);
			if (contaOrigem == null) throw new IllegalArgumentException("O número da conta de origem é inválido.");

    		String sql =   "UPDATE conta_corrente "
    					 + "SET saldo = ? "
    					 + "WHERE numero_conta = ?";

    		switch (tipoTransacao) {
    			case TR_DEPOSITO:
					try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
						pstmt.setDouble(1, contaOrigem.getSaldo() + quantia);
						pstmt.setInt(2, numeroOrigem);
		
						pstmt.executeUpdate();
					}

					registrarTransacao(TR_DEPOSITO, quantia, numeroOrigem, 0);
    				break;
    			case TR_SAQUE:
        			if (quantia > contaOrigem.getSaldo()) {
        				throw new IllegalArgumentException("Saldo insuficiente para saque.");
        			}

					try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
						pstmt.setDouble(1, contaOrigem.getSaldo() - quantia);
						pstmt.setInt(2, numeroOrigem);
		
						pstmt.executeUpdate();
					}

					registrarTransacao(TR_SAQUE, quantia, numeroOrigem, 0);
    				break;
    			case TR_TRANSFERENCIA:
					Conta contaDestino = buscarConta(numeroDestino);
					if (contaDestino == null) {
						throw new IllegalArgumentException("O número da conta de destino é inválido.");
					}
        			if (quantia > contaOrigem.getSaldo()) {
        				throw new IllegalArgumentException("Saldo insuficiente para transferência.");
        			}

        			// Executar duas instruções SQL:
        			// 1º: Sacar da conta origem
        			// 2º: Depositar na conta destino

        			// Aguardar um COMMIT manual para garantir segurança
        			conn.setAutoCommit(false);

					try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
						pstmt.setDouble(1, contaOrigem.getSaldo() - quantia);
						pstmt.setInt(2, numeroOrigem);
						
						pstmt.executeUpdate();

						try (PreparedStatement pstmt2 = conn.prepareStatement(sql)) {
							pstmt2.setDouble(1, contaDestino.getSaldo() + quantia);
							pstmt2.setInt(2, numeroDestino);
							
							pstmt2.executeUpdate();
						} catch (Exception e) {
							conn.rollback();
        					conn.setAutoCommit(true);
						}
					} catch (SQLException e) {
						conn.rollback();
					} finally {
						conn.commit();
        				conn.setAutoCommit(true); // Retomar COMMITs automáticos
					}

					registrarTransacao(TR_TRANSFERENCIA, quantia, numeroOrigem, numeroDestino);
    				break;
    			default:
    				throw new IllegalArgumentException("Tipo de transação inválido.");
    		}
    	}
    }

    public static void registrarTransacao(int tipoTransacao, double quantia, int numeroOrigem, int numeroDestino) throws SQLException {
		try (Connection conn = conectar()) {
			if (conn == null) return;
        	if (quantia <= 0) throw new IllegalArgumentException("O valor para transação deve ser positivo.");
			if (numeroOrigem <= 0) throw new IllegalArgumentException("O número da conta de origem deve ser positivo.");
			if (numeroDestino <= 0) throw new IllegalArgumentException("O número da conta de destino deve ser positivo.");

			String sql =   "INSERT INTO transacoes "
					     + "VALUES (NULL, ?, ?, ?, ?, ?)";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, tipoTransacao);
				pstmt.setDouble(2, quantia);
				pstmt.setInt(3, numeroOrigem);

				// algumas transações não possuem destinatário (saque/depósito)
				if (numeroDestino == 0) {
					pstmt.setNull(4, java.sql.Types.INTEGER);
				} else {
					pstmt.setInt(4, numeroDestino);
				}

				pstmt.setString(5, LocalDateTime.now().toString());
				pstmt.execute();
			}
		}
    }

	public static void deletarConta(int numeroConta) throws SQLException, IllegalArgumentException {
		try (Connection conn = conectar()) {
			if (conn == null) return;
			if (numeroConta <= 0) throw new IllegalArgumentException("O número da conta deve ser positivo.");

			Conta conta = buscarConta(numeroConta);
			if (conta == null) return;
			if (conta.getSaldo() != 0) throw new IllegalArgumentException("O saldo da conta deve ser zero (R$0,00).");

			String sql =   "DELETE FROM conta_corrente "
					     + "WHERE numero_conta = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, numeroConta);

				pstmt.execute();
			}
		}
	}
}