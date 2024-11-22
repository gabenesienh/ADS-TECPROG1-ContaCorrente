//TODO: verificar se buscarHistorico() funciona
//TODO: manual pdf
//TODO: interface gráfica com swing...

//OBS: "quantia" em transações agora é sempre positivo
//OBS: try-catches devem ser feitos aqui agora

import java.sql.SQLException;

public class App {
	public static void main(String args[]) {
		// Inicializar tabelas se ainda não existem
		try {
			ContaDAO.criarTabelas();
		} catch (SQLException e) {
			System.out.println("Erro ao conectar com o banco de dados");
		}

		try {
			// ContaDAO.novaConta(1, "Josicleide Almôndega Maltrapilhos");
			// ContaDAO.novaConta(2, "Jean-Claude Ferdinando Burromuerto Descartes");

			// ContaDAO.executarTransacao(ContaDAO.TR_DEPOSITO, 900, 1, 0);
			// ContaDAO.executarTransacao(ContaDAO.TR_SAQUE, 25, 1, 0);
			// ContaDAO.executarTransacao(ContaDAO.TR_TRANSFERENCIA, 150, 1, 2);
		} catch (Exception e) {
			//TODO: lidar com exceptions
			System.out.println("Oops: " + e.getMessage());
		}
	}
}