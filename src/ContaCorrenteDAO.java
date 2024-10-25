//TODO: método apagar conta (saldo da conta deve ser exatamente 0)

import java.sql.*;
import java.time.LocalDateTime;

public class ContaCorrenteDAO {
	private Connection conectar() throws SQLException {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:database.db");
		} catch (SQLException e) {
			System.out.println("Erro ao conectar ao banco de dados" + e.getMessage());
		}
		return conn;
	}

	public void criarTabelas() {
		try (Connection conn = this.conectar()) {
			if (conn == null) return;

			// Tabela com as contas
			String sql =   "CREATE TABLE IF NOT EXISTS conta_corrente ("
						 + "numero INTEGER PRIMARY KEY, "
						 + "saldo REAL, "
						 + "limite REAL)";

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sql);
			}

			// Tabela com os registros de transações
			sql =   "CREATE TABLE IF NOT EXISTS transacoes ("
				  + "id_transacao INTEGER PRIMARY KEY AUTOINCREMENT, "
				  + "tipo_transacao TEXT, "
				  + "quantia REAL, "
				  + "id_origem INTEGER REFERENCES conta_corrente(numero), "
				  + "id_destinatario INTEGER REFERENCES conta_corrente(numero), "
				  + "data_hora DATETIME)";

			try (Statement stmt = conn.createStatement()) {
				stmt.execute(sql);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	// OBS: para muitas dessas funções, cabe ao app validar os parâmetros

	public ContaCorrente novaConta(int numero, double saldo, double limite) {
		ContaCorrente conta = null;

		try (Connection conn = this.conectar()) {
			if (conn == null) return conta;

			String sql =   "INSERT INTO conta_corrente "
			             + "VALUES (?, ?, ?)";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, numero);
				pstmt.setDouble(2, saldo);
				pstmt.setDouble(3, limite);
	
				pstmt.execute();
				conta = new ContaCorrente(numero, saldo, limite);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return conta;
	}

	public ContaCorrente buscarConta(int numeroConta) {
		ContaCorrente conta = null;

		try (Connection conn = this.conectar()) {
			if (conn == null) return conta;

			String sql =   "SELECT numero, saldo, limite " 
			             + "FROM conta_corrente "
			             + "WHERE numero = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, numeroConta);
	
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					conta = new ContaCorrente(rs.getInt("numero"), rs.getDouble("saldo"), rs.getDouble("limite"));
				}
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return conta;
	}

    public void transferir(double quantia, ContaCorrente origem, ContaCorrente destinatario) {	
        origem.sacar(quantia, false);
        destinatario.depositar(quantia);

        // para esse método, atualizarSaldo é feito aqui e não no app
        // desse jeito, evita que a transferência só seja feita em uma das contas
        this.atualizarSaldo(origem);
        this.atualizarSaldo(destinatario);
        this.registrarTransacao("transfer", -quantia, origem, destinatario);
    }

    public void registrarTransacao(String tipoTransacao, double quantia, ContaCorrente origem, ContaCorrente destinatario) {
		try (Connection conn = this.conectar()) {
			if (conn == null) return;

			String sql =   "INSERT INTO transacoes "
					     + "VALUES (NULL, ?, ?, ?, ?, ?)";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setString(1, tipoTransacao);
				pstmt.setDouble(2, quantia);
				pstmt.setInt(3, origem.getNumero());

				// algumas transações não possuem destinatário (saque/depósito)
				if (destinatario == null) {
					pstmt.setNull(4, java.sql.Types.INTEGER);
				} else {
					pstmt.setInt(4, destinatario.getNumero());
				}

				pstmt.setString(5, LocalDateTime.now().toString());
				pstmt.execute();
			}
		} catch (SQLException e) {
		    System.out.println(e.getMessage());
		}
    }

    public void exibirTransacoes(ContaCorrente conta, int maximoTransacoes) {
		try (Connection conn = this.conectar()) {
			if (conn == null) return;

			String sql =   "SELECT tipo_transacao, quantia, id_origem, id_destinatario, strftime('%d/%m/%Y %T', data_hora) AS data_hora "
			             + "FROM transacoes "
			             + "WHERE id_origem = ? OR id_destinatario = ? "
			             + "ORDER BY id_transacao DESC";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, conta.getNumero());
				pstmt.setInt(2, conta.getNumero());
				
				ResultSet rs = pstmt.executeQuery();
				int i = 0;
				while (rs.next()) {
        	    	if (i == maximoTransacoes) break;

        	    	// sinal "+" ou "-", mostrando se a conta recebeu ou perdeu dinheiro
        	    	boolean sinal = (rs.getDouble("quantia") >= 0);

        	    	// no database, a quantia é salva em relação ao id_origem
        	    	// ou seja, uma transferência é salva como valor negativo
        	    	// se o destinatário for esta mesma conta corrente, inverter o sinal para corrigir
        	    	if (rs.getInt("id_destinatario") == conta.getNumero()) {
						sinal = !sinal;
					}

					String tipoTransacao = rs.getString("tipo_transacao");
					String tipoTransacaoFormatado = "";

					if (tipoTransacao.equals("deposit")) {
						tipoTransacaoFormatado = "Depósito";
					} else if (tipoTransacao.equals("withdrawal")) {
						tipoTransacaoFormatado = "Saque";
					} else if (tipoTransacao.equals("transfer")) {
						if (sinal) {
							tipoTransacaoFormatado = "Transferência recebida";
						} else {
							tipoTransacaoFormatado = "Transferência realizada";
						}
					}

					System.out.println("----------");
					System.out.println(rs.getString("data_hora"));
					System.out.println(tipoTransacaoFormatado);
        	    	System.out.printf("%sR$%.2f%n", //ex.: "+R$35,00"
        	    		sinal ? "+" : "-",
        	    		Math.abs(rs.getDouble("quantia"))
        	    	);
					if (tipoTransacaoFormatado.equals("Transferência recebida")) {
						System.out.println("De: Conta nº" + rs.getInt("id_origem"));
					} else if (tipoTransacaoFormatado.equals("Transferência realizada")) {
						System.out.println("Para: Conta nº" + rs.getInt("id_destinatario"));
					}

					i++;
				}

				System.out.println("----------");
				System.out.println();
			}
		} catch (SQLException e) {
		    System.out.println(e.getMessage());
		}
    }

    public void exibirTransacoes(ContaCorrente conta) {
        exibirTransacoes(conta, -1);
    }

	public void deletarConta(ContaCorrente conta) {
		try (Connection conn = this.conectar()) {
			if (conn == null) return;

			String sql =   "DELETE FROM conta_corrente "
					     + "WHERE numero = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, conta.getNumero());

				pstmt.execute();
			}
		} catch (SQLException e) {
		    System.out.println(e.getMessage());
		}
	}

    // Atualiza o registro da conta no BD com o seu estado atual no app
	public void atualizarSaldo(ContaCorrente conta) {
		try (Connection conn = this.conectar()) {
			if (conn == null) return;

			String sql = "UPDATE conta_corrente SET saldo = ? WHERE numero = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setDouble(1, conta.getSaldo());
				pstmt.setInt(2, conta.getNumero());
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
		    System.out.println(e.getMessage());
		}
	}

    //TODO: implementar chave pix como alternativa

    /*
    public boolean transferir(double quantia, String chavePix) {
        if (quantia < 0) throw new IllegalArgumentException("O valor da transferência deve ser positivo.");
        if (quantia > this.saldo + this.limite) throw new IllegalArgumentException("Saldo insuficiente para transferência.");
        if (chavePix.length() != 11 || !chavePix.matches("[0-9]{11}")) throw new IllegalArgumentException("Chave Pix inválida.");

        ...

        if (!this.chavesPixFavoritas.contains(chavePix)) {
            this.chavesPixFavoritas.add(chavePix);
        }
        this.transacoes.add(-quantia);
        this.saldo -= quantia;
        return true;
    }
    */
}