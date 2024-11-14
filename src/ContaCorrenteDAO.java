//TODO: apagar histórico de transações de contas deletadas

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

	public List<Historico> buscarHistorico(int numeroConta) {
		List<Historico> listaHistorico = new ArrayList<>();
		
		try (Connection conn = this.conectar()) {
			if (conn == null) return null;

			String sql =   "SELECT * "
			             + "FROM transacoes "
			             + "WHERE id_origem = ? OR id_destinatario = ? ";
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
						rs.getInt("id_destinatario"),
						rs.getObject("data_hora", LocalDateTime.class)
					);

					listaHistorico.add(historico);
				}
			}

			return listaHistorico;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return null;
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

	public void deletarConta(ContaCorrente conta) {
		if (conta.getSaldo() != 0) throw new IllegalArgumentException("O saldo da conta deve ser zero (R$0,00).");

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
}