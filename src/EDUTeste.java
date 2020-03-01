import java.util.Scanner;

public class EDUTeste {
	public static void main(String[] args) throws InterruptedException {
		int rc = -222;
		byte[] sensores = null;
		byte[] versao = null;
		//Funcao Java para capturar dados de entrada do teclado
		@SuppressWarnings("resource")
		Scanner inKeys = new Scanner(System.in);
		//Este pedaço apenas mostra uma das funcionalidades do jssc
		//ela obtem a lista de portas seriais existentes no computador
		try {
			rc = EDUJavaDriver.getPortList();
			if (rc < 0) {
				System.out.println("App:getPortList: rc[" + rc + "]Deu erro");
			} else {
				System.out.println("App:getPortList: rc[" + rc + "]OK");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println("App:getPortList: rc[" + rc + "]falhou");
		}
		// ====================
		// Criar Menu de opcoes
		// ====================
		int opcao = 999;
		while (opcao != 0) {
			System.out.println("=================");
			System.out.println("= Escolha Opcao =");
			System.out.println("=================");
			System.out.println(" 0 - Sair");
			System.out.println(" 1 - abrirComunicacao");
			System.out.println(" 2 - fecharComunicacao");
			System.out.println(" 3 - dispensarDocumento");
			System.out.println(" 4 - lerStatusSensores");
			System.out.println(" 5 - lerVersao");
			opcao = inKeys.nextInt(); //Capturando dados do teclado
			if (opcao == 0) {
				System.out.println("App: Saindo do programa");
				break;
			}
			switch (opcao) {
			case 1:
				rc = EDUJavaDriver.abrirComunicacao();
				break;
			case 2:
				rc = EDUJavaDriver.fecharComunicacao();
				break;
			case 3:
				rc = EDUJavaDriver.dispensarDocumento();
				break;
			case 4:
				rc = EDUJavaDriver.lerStatusSensores();
				if (rc >= 0)
				{
					sensores = EDUJavaDriver.getResposta();
					String strStatusSensores = new String(sensores);
					System.out.println("App:lerVersao rc[" + rc + "] strStatusSensores["+ strStatusSensores + "]OK");
				}
				break;
			case 5:
				rc = EDUJavaDriver.lerVersao();
				if (rc >= 0)
				{
					versao = EDUJavaDriver.getResposta();
					String strVersao = new String(versao);
					System.out.println("App:lerVersao rc[" + rc + "] strVersao["+ strVersao + "]OK");
				}
				break;
			}
		}
		System.out.println("App:Saiu do Programa!!!!!!");
	}
}