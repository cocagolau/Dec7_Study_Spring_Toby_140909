package me.dec7.learningtest.template;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Calculator {
	
	/*
	 * p251
	 * 초난감 Calculator 
	 */
	public Integer calcSum1(String filepath) throws IOException {		
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		Integer sum = 0;
		String line = null;
		
		while ((line = br.readLine()) != null) {
			sum += Integer.valueOf(line);
		}
		br.close();
	
		return sum;
	}
	
	/*
	 * p251
	 * 
	 * 파일을 읽거나, 처리 중 예와 발생시
	 *  - 파일이 정상적으로 닫히지 않음
	 * 
	 * 해결책
	 *  - try / finally 구문 적용
	 */
	public Integer calcSum2(String filepath) throws IOException {		
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(filepath));
			Integer sum = 0;
			String line = null;
			
			while ((line = br.readLine()) != null) {
				sum += Integer.valueOf(line);
			}
		
			return sum;
			
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			throw ioe;
			
		} finally {
			
			// BufferedReader 오브젝트가 생성전에 예외 발생할 수도 있으므로 null 체크 필요
			if (br != null) {
				try {
					br.close();
					
				} catch (IOException ioe) {
					System.out.println(ioe.getMessage());
					
				}
			}
		}
	}
	
	/*
	 * 숫자 곱 기능 추가 요구
	 * 
	 * 템플릿/콜백 패턴을 적용해 중복 제거
	 *  - 템플릿	
	 *		- 템플릿에 어떤 내용을 담을 것인가?
	 *		- 템플릿이 콜백에게 전달해줄 내부 정보는?
	 *	- 콜백
	 *		- 콜백이 템플릿에게 되 돌려줄 것은?
	 *	- 템플릿
	 *		- 템플릿이 작업을 마치고 클라이언트에게 전달 할 것은?
	 *
	 * 따라서
	 * 	- 템플릿, 콜백의 경계정하기
	 *  - 템플릿이 콜백에게, 콜백이 템플릿에게 전달하는 내용 정하기
	 *  
	 * 결론
	 *  - 템플릿
	 *  	- 파일을 결과 각 라인을 읽어올 수 있는 BufferedReader를 만들어 콜백에게 전달
	 *  - 콜백
	 *  	- 각 라인을 읽어서 알아서 처리 후 최종 결과만 템플릿에게 전달
	 */
	public Integer calcSum3(String filepath) throws IOException {		
		return fileReadTemplate3(filepath, new BufferedReaderCallback() {

			@Override
			public Integer doSomethingWithReader(BufferedReader br) throws IOException {
				Integer sum = 0;
				String line = null;
				
				while ((line = br.readLine()) != null) {
					sum += Integer.valueOf(line);
				}
				
				return sum;
			}
			
		});
	}
	
	public Integer calcMultiply3(String filepath) throws IOException {
		return fileReadTemplate3(filepath, new BufferedReaderCallback() {

			@Override
			public Integer doSomethingWithReader(BufferedReader br) throws IOException {
				Integer multiply = 1;
				String line = null;
				
				while ((line = br.readLine()) != null) {
					multiply *= Integer.valueOf(line);
				}
				
				return multiply;
			}
			
		});
	}
	
	/*
	 * 분리된 템플릿 메소드
	 * 
	 * 템플릿
	 *  - BufferedReaderCallback interface 타입의 콜백 오브젝트를 받아서 실행
	 *  - 최종 결과는 client에게 전달
	 */
	public Integer fileReadTemplate3(String filepath, BufferedReaderCallback callback) throws IOException {		
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(filepath));
			/*
			 * callback method 호출
			 * template에서 만든 context정보인 BufferedReader를 전달해 주고 작업 결과를 받는다 
			 */
			int ret = callback.doSomethingWithReader(br);
			
			return ret;
			
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			throw ioe;
			
		} finally {
			
			// BufferedReader 오브젝트가 생성전에 예외 발생할 수도 있으므로 null 체크 필요
			if (br != null) {
				try {
					br.close();
					
				} catch (IOException ioe) {
					System.out.println(ioe.getMessage());
					
				}
			}
		}
	}
	
	
	/*
	 * calSum3, calMultiply3의 코드에서 중복 발생 
	 * 다시 템플릿/ 콜팩 패턴 적용
	 * 
	 * 변하는 코드의 경계를 찾기
	 * 그리고 경계를 사이에 두고 주고받는 일정한 정보가 있는지 확인
	 */
	
	// LineCallback을 사용하는 template
	public Integer lineReadTemplate4(String filepath, LineCallback4 callback, int initValue) throws IOException {
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(filepath));
			Integer res = initValue;
			String line = null;
			
			// 각 loop를 도는 행위도 템플릿이 담당
			while ((line = br.readLine()) != null) {
				// 각 라인의 내용을 계산하는 작업만 콜백에게 맡김
				res = callback.doSomethingWithLine(line, res);
			}
			
			return res;
			
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			throw ioe;
			
		} finally {
			
			// BufferedReader 오브젝트가 생성전에 예외 발생할 수도 있으므로 null 체크 필요
			if (br != null) {
				try {
					br.close();
					
				} catch (IOException ioe) {
					System.out.println(ioe.getMessage());
					
				}
			}
		}
		
	}
	
	/*
	 *  재구성된 calcSum, calcMultify
	 *  순수한 계산로직만 남아 관심사가 명확함
	 */
	public Integer calcSum4(String filepath) throws IOException {		
		return lineReadTemplate4(filepath, new LineCallback4() {

			@Override
			public Integer doSomethingWithLine(String line, Integer value) {
				return value += Integer.valueOf(line);
			}
			
		}, 0);
	}
	
	public Integer calcMultiply4(String filepath) throws IOException {		
		return lineReadTemplate4(filepath, new LineCallback4() {

			@Override
			public Integer doSomethingWithLine(String line, Integer value) {
				return value *= Integer.valueOf(line);
			}
			
		}, 0);
	}
	
	
	/*
	 * 지금은 반환결과가 Integer로 고정됨
	 * Generics를 활용하여 다양한 오브젝트를 반환할 수 있음
	 * 
	 * lineReadTemplate5 메소드는 아래 파라미터를 받음
	 *  - T타입 파라미터를 갖는 interface LineCallback
	 *  - T타입 초기값 initValue 
	 *  
	 * T타입 res 정의
	 * T타입 파라미터로 선언된 LineCallback의 메소드 호출하여 처리 후
	 * T타입의 결과를 반환
	 */
	
	public <T> T lineReadTemplate5(String filepath, LineCallback5<T> callback, T initValue) throws IOException {
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(filepath));
			T res = initValue;
			String line = null;
			
			// 각 loop를 도는 행위도 템플릿이 담당
			while ((line = br.readLine()) != null) {
				// 각 라인의 내용을 계산하는 작업만 콜백에게 맡김
				res = callback.doSomethingWithLine(line, res);
			}
			
			return res;
			
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			throw ioe;
			
		} finally {
			
			// BufferedReader 오브젝트가 생성전에 예외 발생할 수도 있으므로 null 체크 필요
			if (br != null) {
				try {
					br.close();
					
				} catch (IOException ioe) {
					System.out.println(ioe.getMessage());
					
				}
			}
		}
	}
	
	public Integer calcSum5(String filepath) throws IOException {		
		return lineReadTemplate5(filepath, new LineCallback5<Integer>() {

			@Override
			public Integer doSomethingWithLine(String line, Integer value) {
				return value += Integer.valueOf(line);
			}
			
		}, 0);
	}
	
	public Integer calcMultiply5(String filepath) throws IOException {		
		return lineReadTemplate5(filepath, new LineCallback5<Integer>() {

			@Override
			public Integer doSomethingWithLine(String line, Integer value) {
				return value *= Integer.valueOf(line);
			}
			
		}, 1);
	}
	
	public String concatenate5(String filepath) throws IOException {		
		return lineReadTemplate5(filepath, new LineCallback5<String>() {

			@Override
			public String doSomethingWithLine(String line, String value) {
				return value + line;
			}
			
		}, "");
	}

}
